package com.github.engarik.pdfconverterbot.image.impl;

class ImageInformation {
    public final int orientation;
    public int width;
    public int height;

    ImageInformation(int orientation, int width, int height) {
        this.orientation = orientation;
        this.width = width;
        this.height = height;
    }

}
