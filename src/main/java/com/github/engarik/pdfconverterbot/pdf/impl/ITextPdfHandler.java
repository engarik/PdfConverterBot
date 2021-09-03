package com.github.engarik.pdfconverterbot.pdf.impl;

import com.github.engarik.pdfconverterbot.utils.ImageFileFilter;
import com.github.engarik.pdfconverterbot.pdf.PdfHandler;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.AreaBreakType;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ITextPdfHandler implements PdfHandler {

    private final FilenameFilter imageFileFilter;

    public ITextPdfHandler() {
        imageFileFilter = new ImageFileFilter();
    }

    @Override
    public void convert(File directory) throws Exception {
        convert(directory, "output.pdf");
    }

    @Override
    public void convert(File directory, String outputFilename) throws Exception {
        if (!(directory.exists() || directory.isDirectory())) {
            throw new IllegalArgumentException("rootDirectory must exist and be a directory.");
        }
        Optional<File[]> listOfImageFiles = Optional.ofNullable(directory.listFiles(imageFileFilter));
        if (listOfImageFiles.isPresent()) {
            List<File> imageFiles = new ArrayList<>(Arrays.asList(listOfImageFiles.get()));

            PdfWriter pdfWriter = new PdfWriter(new File(directory, outputFilename));
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);


            boolean notFirst = false;
            for (File imageFile : imageFiles) {
                ImageData imageData = ImageDataFactory.create(imageFile.getAbsolutePath());
                Image image = new Image(imageData);

                image.setFixedPosition(0, 0);

                if (image.getImageWidth() > image.getImageHeight()) {
                    pdfDocument.setDefaultPageSize(PageSize.A4.rotate());
                    image.scaleAbsolute(PageSize.A4.rotate().getWidth(), PageSize.A4.rotate().getHeight());
                } else {
                    pdfDocument.setDefaultPageSize(PageSize.A4);
                    image.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());
                }

                if (notFirst) {
                    document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                }

                document.add(image);
                notFirst = true;
            }
            document.close();
            pdfDocument.close();
        }
    }
}
