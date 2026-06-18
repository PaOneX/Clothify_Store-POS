package edu.icet.util;

import edu.icet.model.dto.DailySalesDto;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public final class CsvExportUtil {

    private CsvExportUtil() {
    }

    public static void exportDailySales(File file, Collection<DailySalesDto> rows) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Date,Orders,Revenue\n");
            for (DailySalesDto row : rows) {
                writer.write(String.format("%s,%d,%.2f%n",
                        row.getSaleDate(), row.getOrderCount(), row.getRevenue()));
            }
        }
    }
}
