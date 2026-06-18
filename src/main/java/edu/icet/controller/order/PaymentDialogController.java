package edu.icet.controller.order;

import edu.icet.model.dto.CheckoutRequestDto;
import edu.icet.model.dto.OrderPaymentDto;
import edu.icet.model.enums.PaymentMethod;
import edu.icet.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PaymentDialogController implements Initializable {

    private static final double AMOUNT_TOLERANCE = 0.01;

    private double subtotal;
    private double discount;
    private double tax;
    private double total;

    private boolean confirmed;
    private CheckoutRequestDto checkoutRequest;

    private final List<SplitRow> splitRows = new ArrayList<>();

    @FXML private VBox dialogRoot;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDiscount;
    @FXML private Label lblTax;
    @FXML private Label lblTotal;
    @FXML private RadioButton rbCash;
    @FXML private RadioButton rbCard;
    @FXML private RadioButton rbBankTransfer;
    @FXML private RadioButton rbSplit;
    @FXML private VBox cashPane;
    @FXML private TextField txtAmountReceived;
    @FXML private Label lblChangeGiven;
    @FXML private VBox splitPane;
    @FXML private VBox splitRowsContainer;
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ToggleGroup paymentGroup = new ToggleGroup();
        rbCash.setToggleGroup(paymentGroup);
        rbCard.setToggleGroup(paymentGroup);
        rbBankTransfer.setToggleGroup(paymentGroup);
        rbSplit.setToggleGroup(paymentGroup);
        rbCash.setSelected(true);

        paymentGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> updatePaymentPanes());

        txtAmountReceived.textProperty().addListener((obs, oldVal, newVal) -> updateChangeGiven());
        txtAmountReceived.setOnAction(e -> btnConfirm.fire());

        addSplitRow();
        updatePaymentPanes();
        updateChangeGiven();
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        dialogRoot.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                btnCancel.fire();
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER && !txtAmountReceived.isFocused()) {
                btnConfirm.fire();
                e.consume();
            } else if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT) {
                cyclePaymentMethod(e.getCode() == KeyCode.RIGHT ? 1 : -1);
                e.consume();
            }
        });
    }

    private void cyclePaymentMethod(int direction) {
        RadioButton[] buttons = {rbCash, rbCard, rbBankTransfer, rbSplit};
        int current = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].isSelected()) { current = i; break; }
        }
        int next = (current + direction + buttons.length) % buttons.length;
        buttons[next].setSelected(true);
    }

    public void setTotals(double subtotal, double discount, double tax, double total) {
        this.subtotal = subtotal;
        this.discount = discount;
        this.tax = tax;
        this.total = total;

        lblSubtotal.setText(formatMoney(subtotal));
        lblDiscount.setText(formatMoney(discount));
        lblTax.setText(formatMoney(tax));
        lblTotal.setText(formatMoney(total));

        updateChangeGiven();
    }

    public CheckoutRequestDto getCheckoutRequest() {
        return checkoutRequest;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    void btnConfirm(ActionEvent event) {
        if (!validate()) {
            return;
        }
        checkoutRequest = buildCheckoutRequest();
        confirmed = true;
        closeDialog();
    }

    @FXML
    void btnCancel(ActionEvent event) {
        confirmed = false;
        checkoutRequest = null;
        closeDialog();
    }

    @FXML
    void btnAddSplitRow(ActionEvent event) {
        addSplitRow();
    }

    @FXML
    void btnRemoveSplitRow(ActionEvent event) {
        if (splitRows.size() <= 1) {
            AlertUtil.showWarning("Split Payment", "At least one split payment row is required.");
            return;
        }
        SplitRow row = splitRows.remove(splitRows.size() - 1);
        splitRowsContainer.getChildren().remove(row.container);
    }

    private void updatePaymentPanes() {
        PaymentMethod method = getSelectedMethod();
        boolean cash = method == PaymentMethod.CASH;
        boolean split = method == PaymentMethod.SPLIT;
        cashPane.setVisible(cash);
        cashPane.setManaged(cash);
        splitPane.setVisible(split);
        splitPane.setManaged(split);
    }

    private void updateChangeGiven() {
        double received = parseAmount(txtAmountReceived.getText());
        double change = Math.max(0, received - total);
        lblChangeGiven.setText(formatMoney(change));
    }

    private PaymentMethod getSelectedMethod() {
        if (rbCard.isSelected()) {
            return PaymentMethod.CARD;
        }
        if (rbBankTransfer.isSelected()) {
            return PaymentMethod.BANK_TRANSFER;
        }
        if (rbSplit.isSelected()) {
            return PaymentMethod.SPLIT;
        }
        return PaymentMethod.CASH;
    }

    private boolean validate() {
        PaymentMethod method = getSelectedMethod();

        if (method == PaymentMethod.CASH) {
            double received = parseAmount(txtAmountReceived.getText());
            if (received < total - AMOUNT_TOLERANCE) {
                AlertUtil.showWarning("Payment", "Insufficient cash received.");
                txtAmountReceived.requestFocus();
                return false;
            }
            return true;
        }

        if (method == PaymentMethod.SPLIT) {
            if (splitRows.isEmpty()) {
                AlertUtil.showWarning("Split Payment", "Add at least one payment row.");
                return false;
            }

            double sum = 0;
            for (SplitRow row : splitRows) {
                if (row.methodBox.getValue() == null) {
                    AlertUtil.showWarning("Split Payment", "Select a payment method for each row.");
                    return false;
                }
                double amount = parseAmount(row.amountField.getText());
                if (amount <= 0) {
                    AlertUtil.showWarning("Split Payment", "Each split amount must be greater than zero.");
                    row.amountField.requestFocus();
                    return false;
                }
                sum += amount;
            }

            if (Math.abs(sum - total) > AMOUNT_TOLERANCE) {
                AlertUtil.showWarning("Split Payment",
                        String.format("Split payments must equal total (%s). Current sum: %s.",
                                formatMoney(total), formatMoney(sum)));
                return false;
            }
            return true;
        }

        return true;
    }

    private CheckoutRequestDto buildCheckoutRequest() {
        CheckoutRequestDto request = new CheckoutRequestDto();
        PaymentMethod method = getSelectedMethod();
        request.setPaymentMethod(method);

        if (method == PaymentMethod.CASH) {
            request.setAmountReceived(parseAmount(txtAmountReceived.getText()));
        } else if (method == PaymentMethod.SPLIT) {
            List<OrderPaymentDto> payments = new ArrayList<>();
            for (SplitRow row : splitRows) {
                OrderPaymentDto payment = new OrderPaymentDto();
                payment.setMethod(row.methodBox.getValue());
                payment.setAmount(parseAmount(row.amountField.getText()));
                payments.add(payment);
            }
            request.setSplitPayments(payments);
        }

        return request;
    }

    private void addSplitRow() {
        ComboBox<PaymentMethod> methodBox = new ComboBox<>(
                FXCollections.observableArrayList(PaymentMethod.CASH, PaymentMethod.CARD, PaymentMethod.BANK_TRANSFER));
        methodBox.getSelectionModel().selectFirst();
        methodBox.setPrefWidth(160);
        methodBox.getStyleClass().add("modern-input");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.getStyleClass().add("modern-input");
        HBox.setHgrow(amountField, Priority.ALWAYS);

        Label methodLabel = new Label("Method");
        methodLabel.setMinWidth(56);
        Label amountLabel = new Label("Amount");
        amountLabel.setMinWidth(56);

        HBox row = new HBox(10, methodLabel, methodBox, amountLabel, amountField);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));

        splitRows.add(new SplitRow(row, methodBox, amountField));
        splitRowsContainer.getChildren().add(row);
    }

    private double parseAmount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatMoney(double value) {
        return String.format("Rs. %.2f", value);
    }

    private void closeDialog() {
        Stage stage = (Stage) dialogRoot.getScene().getWindow();
        stage.close();
    }

    private static final class SplitRow {
        private final HBox container;
        private final ComboBox<PaymentMethod> methodBox;
        private final TextField amountField;

        private SplitRow(HBox container, ComboBox<PaymentMethod> methodBox, TextField amountField) {
            this.container = container;
            this.methodBox = methodBox;
            this.amountField = amountField;
        }
    }
}
