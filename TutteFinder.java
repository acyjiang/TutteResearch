import java.util.*;
import java.io.*;

public class TutteFinder {
	public static void main (String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("graph.txt"));
		
		int numGraphs = Integer.parseInt(br.readLine());

		Graph[] graphs = new Graph[numGraphs]; // stores the graphs

		for (int ct = 0; ct < numGraphs; ct++) { // repeat process for each graph
			int v = Integer.parseInt(br.readLine()); // number of vertices
			int e = Integer.parseInt(br.readLine()); // number of edges
			
			ArrayList<Integer>[] adj  = new ArrayList[v]; // adjacency list. row i stores the neighbors of vertex i
			for (int i = 0; i < v; i++) {
				adj[i] = new ArrayList<Integer>();
			}
			int[][] edges = new int[e][2]; // edge list. row i stores the 2 ends of the ith edge
			
			StringTokenizer st;
			for (int i = 0; i < e; i++) {
				st = new StringTokenizer(br.readLine());
				int a = Integer.parseInt(st.nextToken());
				int b = Integer.parseInt(st.nextToken());
				adj[a].add(b);
				adj[b].add(a);
				edges[i] = new int[] {Math.min(a, b), Math.max(a, b)};
			}
			
			graphs[ct] = new Graph(v, adj, edges); // new graph object
			
			System.out.printf("------------Graph %d---------------\n", ct+1);

			graphs[ct].printAdj(); // print adjacency list
			
			System.out.println("************Tutte Strings************");
			
			ArrayList<String> sequences = graphs[ct].tutte; // store tutte sequences
			
			for (String s : sequences) { // print tutte sequences
				System.out.println(s);
			}
			
			Graph h = bipartite(graphs[ct]);
			
			System.out.printf("------------Bipartite of Graph %d---------------\n", ct+1);

			h.printAdj(); // print adjacency list
			
			System.out.println("************Tutte Strings************");
			
			ArrayList<String> hsequences = h.tutte; // store tutte sequences
			
			for (String s : hsequences) { // print tutte sequences
				System.out.println(s);
			}
			
			System.out.println(hsequences.size());
			
		}
		
	}
	
	static class Graph {
		int v; // num vertices
		int e; // num edges
		ArrayList<Integer>[] adj; // adjacency list
		int[][] edges; // edge pairs (e by 2 matrix). loops allowed.
		int[] root; // used for contraction process. ex: if vertex 1 is contracted onto vertex 0, instead of updating edges from (1, 2) to (0, 2), make 1 map to 0.
		ArrayList<String> tutte; // tutte polynomial of the graph
		
		public Graph (int v, ArrayList<Integer>[] adj, int[][] edges) { // constructor 
			this.v = v;
			this.adj = adj;
			this.edges = edges;
			this.e = edges.length;
			
			root = new int[v];
			for (int i = 0; i < v; i++) {
				root[i] = i; // each vertex maps to itself when nothing has been contracted
			}
			this.tutte = tutte(); // upon initialization, create the tutte polynomial and store it as an attribute of the graph
		}
		
		public Graph (Graph g) { // con
			v = g.v;
			adj = new ArrayList[v];
			for (int i = 0; i < v; i++) {
				adj[i] = new ArrayList<Integer>();
				for (int j : g.adj[i]) {
					adj[i].add(j);
				}
			}
			
			edges = new int[g.e][2];
			for (int i = 0; i < g.e; i++) {
				for (int j = 0; j < 2; j++) {
					edges[i][j] = g.edges[i][j];
				}
			}
			this.e = g.e;
			
			this.root = new int[v];
			for (int i = 0; i < v; i++) {
				this.root[i] = g.root[i];
			}
			this.tutte = g.tutte;
		}
		
		public void printAdj() {
			System.out.println("*******Adjacency list:*******");
			for (int i = 0; i < v; i++) {
				System.out.print(i + ": ");
				for (int t : adj[i]) {
					System.out.print(t + " " );
				}
				System.out.println();
			}
		}
		
		public int find(int v1) {
			if (v1 == root[v1]) {
				return v1;
			}
			return root[v1] = find(root[v1]);
		}
		
		public boolean isEdge (int edge) {
			int a = find(edges[edge][0]);
			int b = find(edges[edge][1]); 
			if (!adj[a].contains(b) || !adj[b].contains(a)) {
				return false;
			}
			return true;
		}
		
		
		public boolean isIsthmus (int edge) {
			int a = find(edges[edge][0]);
			int b = find(edges[edge][1]);
			
			if (!isEdge(edge)) {
				return false;
			}
			
			adj[a].remove(adj[a].indexOf(b));
			adj[b].remove(adj[b].indexOf(a));
			
			boolean[] vis = new boolean[v];
			Arrays.fill(vis, false);
			LinkedList<Integer> ll = new LinkedList<Integer>();
			ll.add(a);
			vis[a] = true;
			
			while (!ll.isEmpty()) {
				int t = ll.poll();
				for (int t1 : adj[t]) {
					if (!vis[t1]) {
						ll.add(t1);
						vis[t1] = true;
					}
				}
			}
			
			adj[a].add(b);
			adj[b].add(a);

			if (vis[b]) {
				return false;
			} else {
				return true;
			}
		}
		
		public boolean isLoop (int edge) {
			int a = find(edges[edge][0]);
			int b = find(edges[edge][1]);
			
			if (!isEdge(edge)) {
				return false;
			}
			
			if (a == b) {
				return true;
			} else {
				return false;
			}
		}
		
		public boolean delete (int edge) {
			int a = find(edges[edge][0]);
			int b = find(edges[edge][1]);
			
			if (!isEdge(edge)) {
				return false;
			}
			
			adj[a].remove(adj[a].indexOf(b));
			adj[b].remove(adj[b].indexOf(a));
			
			return true;
		}
		
		public boolean contract (int edge) {
			int a = find(edges[edge][0]);
			int b = find(edges[edge][1]);
			
			if (!isEdge(edge)) {
				return false;
			}
			
			adj[a].remove(adj[a].indexOf(b));
			adj[b].remove(adj[b].indexOf(a));
			
			adj[a].addAll(adj[b]);
			adj[b].clear();
			
			root[b] = a;
			
			for (int i = 0; i < v; i++) {
				for (int j = 0; j < adj[i].size(); j++) {
					if (adj[i].get(j) == b) {
						adj[i].set(j,  a);
					}
				}
			}
			
			return true;
		}
		
		
		public ArrayList<String> tutte() {
			return tutte_helper(0, this);
		}
		
		public ArrayList<String> tutte_helper(int cur, Graph g) {
			ArrayList<String> ret = new ArrayList<String>();
			
			if (cur == g.e - 1) {
				if (g.isIsthmus(cur)) {
					ret.add("x");
				} else if (g.isLoop(cur)) {
					ret.add("y");
				}
				return ret;
			}
			
			if (g.isIsthmus(cur)) {
				ArrayList<String> t = tutte_helper(cur + 1, g);
				for (String s : t) {
					ret.add("x" + s);
				}
			} else if (g.isLoop(cur)) {
				ArrayList<String> t = tutte_helper(cur + 1, g);
				for (String s : t) {
					ret.add("y" + s);
				}
			} else {
				Graph gdel = new Graph(g);
				gdel.delete(cur);
				
				ArrayList<String> td = tutte_helper(cur + 1, gdel);
				for (String s : td) {
					ret.add("d" + s);
				}
				
				Graph gcon = new Graph(g);
				gcon.contract(cur);
				
				ArrayList<String> tc = tutte_helper(cur + 1, gcon);
				for (String s : tc) {
					ret.add("c" + s);
				}
			}
			
			return ret;
		}
		
	}
	
	static Graph bipartite(Graph g) {
		int hv = g.v + g.e; // num vertices
		
		ArrayList<Integer>[] hadj = new ArrayList[hv]; // adjacency list
		for (int i = 0; i < hv; i++) {
			hadj[i] = new ArrayList<Integer>();
		}
		
		int[][] hedges = new int[g.e*2][2];
		
		for (int i = 0; i < g.e; i++) {
			int a = g.edges[i][0];
			int b = g.edges[i][1];
			hadj[i + g.v].add(a);
			hadj[i + g.v].add(b);
			hadj[a].add(i + g.v);
			hadj[b].add(i + g.v);
			
			hedges[i * 2][0] = a;
			hedges[i * 2][1] = i + g.v;
			hedges[i * 2 + 1][0] = b;
			hedges[i * 2 + 1][1] = i + g.v;
		}
		
		return new Graph(hv, hadj, hedges);
	}
	
}
