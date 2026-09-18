package xyz.briancorbin.pk.hex.persistence;

/** A store failure surfaced from the adapter; the port stays free of checked JDBC exceptions. */
public final class PersistenceException extends RuntimeException {
  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
