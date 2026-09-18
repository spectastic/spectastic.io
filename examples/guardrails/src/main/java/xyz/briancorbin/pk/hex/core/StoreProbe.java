package xyz.briancorbin.pk.hex.core;

/** Readiness port (006-health-check): can the position store answer a trivial query right now? */
public interface StoreProbe {
  boolean reachable();
}
