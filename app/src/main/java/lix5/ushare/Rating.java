package lix5.ushare;

public class Rating {
    private String star;
    private String message;

    public Rating() {
    }

    public Rating(String star, String message) {
        this.star = star;
        this.message = message;
    }

    public String getStar() {
        return star;
    }

    public String getMessage() {
        return message;
    }

}
