package org.chequePrinter.model;

public class PdfContent {
    public String text;
    public float fontSize;
    public int alignment;
    public float x, y, width, height;

    public PdfContent(String text, float fontSize, int alignment, float x, float y, float width, float height) {
        this.text = text;
        this.fontSize = fontSize;
        this.alignment = alignment;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}