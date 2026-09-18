package xyz.briancorbin.pk.hex.core;

/**
 * The fact that a position moved: instrument, new quantity, originating tradeId (spec 002, FR-001).
 */
public record PositionChanged(String instrument, long quantity, String tradeId) {}
