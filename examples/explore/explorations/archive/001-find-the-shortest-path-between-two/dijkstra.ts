// Second swing: Dijkstra. Once the probe showed BFS taking the 2-hop route that
// costs 25, the answer was obvious — "shortest" here means least WEIGHT, and that's
// exactly what Dijkstra minimises. No negative weights in scope, so no need for
// Bellman-Ford. A plain linear-scan frontier is fine at this size (a binary heap is
// the obvious harden-later step, noted for the plan).

import type { Graph, Node } from './graph.ts';

/** Shortest path by TOTAL WEIGHT. Returns null if goal is unreachable. */
export function dijkstraPath(g: Graph, start: Node, goal: Node): Node[] | null {
  const dist = new Map<Node, number>([[start, 0]]);
  const prev = new Map<Node, Node | null>([[start, null]]);
  const frontier = new Set<Node>([start]);

  while (frontier.size) {
    // pop the frontier node with the smallest known distance (linear scan)
    let node: Node | null = null;
    let best = Infinity;
    for (const n of frontier) {
      const d = dist.get(n) ?? Infinity;
      if (d < best) { best = d; node = n; }
    }
    if (node === null || node === goal) break;
    frontier.delete(node);

    for (const { to, weight } of g.get(node) ?? []) {
      const alt = best + weight;
      if (alt < (dist.get(to) ?? Infinity)) {
        dist.set(to, alt);
        prev.set(to, node);
        frontier.add(to);
      }
    }
  }

  if (!prev.has(goal)) return null;
  const path: Node[] = [];
  for (let at: Node | null = goal; at !== null; at = prev.get(at)!) path.unshift(at);
  return path;
}
