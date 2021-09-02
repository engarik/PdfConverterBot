package com.github.engarik.pdfconverterbot.image.impl;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.github.engarik.pdfconverterbot.image.ImageHandler;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class AffineImageHandler implements ImageHandler {

    @Override
    public void handle(File imageFile) throws Exception {
        // Load image from drive
        BufferedImage image = ImageIO.read(imageFile);
        // Extract EXIF-metadata from file
        ImageInformation imageInformation =
                Optional.ofNullable(readImageData(imageFile)).orElse(new ImageInformation(1, image.getWidth(), image.getHeight()));
        // Determine what operation should be applied based on EXIF-metadata
        AffineTransformOp rotateOp = new AffineTransformOp(getTransformation(imageInformation), AffineTransformOp.TYPE_BILINEAR);
        // Create output image
        BufferedImage output = new BufferedImage(imageInformation.width, imageInformation.height, image.getType());
        // Apply operation
        rotateOp.filter(image, output);
        // Save output image on drive
        File outputFile = new File(imageFile.getParent() + "/" + imageFile.getName());
        outputFile.mkdirs();
        ImageIO.write(output, "JPG", outputFile);
        System.out.println("Image processed");
    }

    private ImageInformation readImageData(File imageFile) throws ImageProcessingException, IOException, MetadataException {
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
        Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);

        int orientation = 1;
        if (Objects.nonNull(directory)) {
            if (directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
            System.out.println(orientation);
            return new ImageInformation(orientation, jpegDirectory.getImageWidth(), jpegDirectory.getImageHeight());
        } else {
            return null;
        }
    }

    private AffineTransform getTransformation(ImageInformation info) {
        switch (info.orientation) {
            case 1:
                break;
            case 2:
                return flipX(info);
            case 3:
                return rotate180(info);
            case 4:
                return flipY(info);
            case 5:
                // TODO: ADD
                break;
            case 6:
                return rotateClockwise90(info);
            case 7:
                // TODO: ADD
                break;
            case 8:
                return rotateCounterClockwise90(info);
        }

        return new AffineTransform();
    }

    private AffineTransform rotateClockwise90(ImageInformation imageInformation) {
        AffineTransform transform = AffineTransform.getRotateInstance(Math.PI / 2);
        transform.translate(0, -imageInformation.height);
        int tmp = imageInformation.width;
        imageInformation.width = imageInformation.height;
        imageInformation.height = tmp;
        return transform;
    }

    private AffineTransform rotateCounterClockwise90(ImageInformation imageInformation) {
        AffineTransform transform = AffineTransform.getRotateInstance(-Math.PI / 2);
        transform.translate(-imageInformation.width, 0);
        int tmp = imageInformation.width;
        imageInformation.width = imageInformation.height;
        imageInformation.height = tmp;
        return transform;
    }

    private AffineTransform rotate180(ImageInformation imageInformation) {
        return AffineTransform.getRotateInstance(Math.PI, imageInformation.width / 2.0, imageInformation.height / 2.0);
    }

    private AffineTransform flipX(ImageInformation imageInformation) {
        AffineTransform transform = AffineTransform.getScaleInstance(-1.0, 1.0);
        transform.translate(-imageInformation.width, 0);
        return transform;
    }

    private AffineTransform flipY(ImageInformation imageInformation) {
        AffineTransform transform = AffineTransform.getScaleInstance(1.0, -1.0);
        transform.translate( 0, -imageInformation.height);
        return transform;
    }

}
