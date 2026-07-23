// First swing: breadth-first search. My gut said "shortest path = fewest steps",
// and BFS is the textbook fewest-edges answer. It ignores weight entirely.

import type { Graph, Node } from './graph.ts';

/** Shortest path by EDGE COUNT (weight-blind). */
export function bfsPath(g: Graph, start: Node, goal: Node): Node[] | null {
  const queue: Node[] = [start];
  const prev = new Map<Node, Node | null>([[start, null]]);
  while (queue.length) {
    const node = queue.shift()!;
    if (node === goal) break;
    for (const { to } of g.get(node) ?? []) {
      if (prev.has(to)) continue;
      prev.set(to, node);
      queue.push(to);
    }
  }
  if (!prev.has(goal)) return null;
  const path: Node[] = [];
  for (let at: Node | null = goal; at !== null; at = prev.get(at)!) path.unshift(at);
  return path;
}
