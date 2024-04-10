package mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations;

public enum FormatType {
    HTML("html"),
    MOODLE_AUTO_FORMAT("moodle_auto_format"),
    PLAIN_TEXT("plain_text"),
    MARKDOWN("markdown");

    private final String format;

    FormatType(String format) {
        this.format = format;
    }

    public String getStringFromFormatType() {
        return this.format;
    }

    public static FormatType getFormatTypeFromString(String format) {
        for (FormatType f : values()) {
            if (f.format.equals(format)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown format: " + format);
    }
}