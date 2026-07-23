// Loose spike — a tiny undirected weighted graph to feel out shortest-path.
// Not a committed contract; just enough to learn which algorithm the problem needs.

export type Node = string;
export interface Edge { to: Node; weight: number; }
export type Graph = Map<Node, Edge[]>;

/** Build an undirected graph from weighted pairs. */
export function graph(pairs: [Node, Node, number][]): Graph {
  const g: Graph = new Map();
  const link = (a: Node, b: Node, w: number) => {
    if (!g.has(a)) g.set(a, []);
    g.get(a)!.push({ to: b, weight: w });
  };
  for (const [a, b, w] of pairs) { link(a, b, w); link(b, a, w); }
  return g;
}

// The sample I kept coming back to. The cheap route (A→B→C→D→E, cost 6) takes the
// MOST hops; the fewest-hops route (A→C→E, 2 hops) is the most expensive (cost 25).
// That gap is the whole question: does "shortest" mean hops or weight?
export const SAMPLE = graph([
  ['A', 'B', 1],
  ['A', 'C', 5],
  ['B', 'C', 1],
  ['B', 'D', 15],
  ['C', 'D', 3],
  ['C', 'E', 20],
  ['D', 'E', 1],
]);

/** Sum the weight of a path (A→B→…); returns Infinity if an edge is missing. */
export function cost(g: Graph, path: Node[]): number {
  let total = 0;
  for (let i = 0; i < path.length - 1; i++) {
    const edge = g.get(path[i])?.find((e) => e.to === path[i + 1]);
    if (!edge) return Infinity;
    total += edge.weight;
  }
  return total;
}
