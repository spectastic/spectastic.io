// The probe that settled it. Run both approaches on the same graph and print the
// path + its real weight. If BFS and Dijkstra disagree, "shortest" is ambiguous and
// weight is what we mean. This is the fact I want to carry into a graduated spec.
//   run:  node probe.ts

import { SAMPLE, cost } from './graph.ts';
import { bfsPath } from './bfs.ts';
import { dijkstraPath } from './dijkstra.ts';

const show = (label: string, path: string[] | null) =>
  console.log(
    `${label.padEnd(9)} ${path ? path.join(' → ') : '(no path)'}` +
      (path ? `   cost=${cost(SAMPLE, path)}` : ''),
  );

console.log('shortest path A → E on the weighted sample:\n');
const bfs = bfsPath(SAMPLE, 'A', 'E');
const dij = dijkstraPath(SAMPLE, 'A', 'E');
show('BFS', bfs);
show('Dijkstra', dij);

const bfsCost = bfs ? cost(SAMPLE, bfs) : Infinity;
const dijCost = dij ? cost(SAMPLE, dij) : Infinity;

console.log('\nlearned:');
console.log(`  BFS minimises HOPS (${bfs?.length! - 1}) — not weight; it costs ${bfsCost}.`);
console.log(`  Dijkstra minimises WEIGHT — the actual shortest path costs ${dijCost}.`);
console.log(`  they disagree by ${bfsCost - dijCost}, so "shortest" must mean least weight.`);

// a loose assertion — enough to prove the point, not a test suite yet
if (dijCost >= bfsCost) {
  console.error('\nUNEXPECTED: Dijkstra should be strictly cheaper here');
  process.exit(1);
}
console.log('\nverdict: keep Dijkstra. graduate as tracer-bullet.');
