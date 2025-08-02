package org.chequePrinter.model;

import java.util.List;
import java.util.Map;

public class BankTemplate {
    private String name;
    private List<Template> templates;

    public String getName() {
        return name;
    }

    public List<Template> getTemplates() {
        return templates;
    }
    
    @Override
    public String toString() {
        return name;
    }

    public static class Template {
        private String templateName;
        private String imagePath;
        private float width;
        private float height;
        private Map<String, Field> fields;

        public String getTemplateName() {
            return templateName;
        }

        public String getImagePath() {
            return imagePath;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }

        public Map<String, Field> getFields() {
            return fields;
        }
        
        @Override
        public String toString() {
            return templateName;
        }
    }

    public static class Field {
        private int x;
        private int y;
        private int fontSize;
        private int alignment;
        private int width;

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getFontSize() {
            return fontSize;
        }

        public int getAlignment() {
            return alignment;
        }

        public int getWidth() {
            return width;
        }
    }
}
