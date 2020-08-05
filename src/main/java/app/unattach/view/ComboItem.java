package app.unattach.view;

public class ComboItem<T> {
  private final String caption;
  public final T value;

  ComboItem(String caption, T value) {
    this.caption = caption;
    this.value = value;
  }

  @Override
  public String toString() {
    return caption;
  }
}