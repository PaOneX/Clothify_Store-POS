package edu.icet.factory;

import edu.icet.service.JasperReportService;
import edu.icet.service.Impl.JasperReportServiceImpl;

public final class DesktopServiceFactory {

    private static DesktopServiceFactory instance;
    private final JasperReportService jasperReportService = new JasperReportServiceImpl();

    private DesktopServiceFactory() {
    }

    public static DesktopServiceFactory getInstance() {
        if (instance == null) {
            instance = new DesktopServiceFactory();
        }
        return instance;
    }

    public JasperReportService getJasperReportService() {
        return jasperReportService;
    }
}
