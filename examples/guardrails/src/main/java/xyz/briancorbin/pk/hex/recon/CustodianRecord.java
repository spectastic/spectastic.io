package xyz.briancorbin.pk.hex.recon;

/** One line of the custodian's end-of-day file: their settled quantity for an instrument. */
public record CustodianRecord(String instrument, long quantity) {}
