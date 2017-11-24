dtmc

label "elected" = s1=3&s2=3&s3=3&s4=3;

const int N = 4;
const int K = 6;
const int Rewards;

module counter

	c : [1..N-1];

	[read] c<N-1 -> (c'=c+1);
	[read] c=N-1 -> (c'=c);
	[done] u1|u2|u3|u4 -> (c'=c);
	[retry] !(u1|u2|u3|u4) -> (c'=1);
	[loop] s1=3 -> (c'=c);

endmodule

module process1

	s1 : [0..3];
	u1 : bool;
	v1 : [0..K-1];
	p1 : [0..K-1];
    n1 : [0..Rewards];

	[pick] s1=0 & n1<Rewards -> 1/K : (s1'=1) & (p1'=0) & (v1'=0) & (u1'=true) & (n1' = n1+1) + 1/K : (s1'=1) & (p1'=1) & (v1'=1) & (u1'=true) & (n1' = n1+1) + 1/K : (s1'=1) & (p1'=2) & (v1'=2) & (u1'=true) & (n1' = n1+1) + 1/K : (s1'=1) & (p1'=3) & (v1'=3) & (u1'=true) & (n1' = n1+1) + 1/K : (s1'=1) & (p1'=4) & (v1'=4) & (u1'=true) & (n1' = n1+1) + 1/K : (s1'=1) & (p1'=5) & (v1'=5) & (u1'=true) & (n1' = n1+1);
	[read] s1=1&u1&c<N-1 -> (u1'=(p1!=v2)) & (v1'=v2);
	[read] s1=1&!u1&c<N-1 -> (u1'=false) & (v1'=v2) & (p1'=0);
	[read] s1=1&u1&c=N-1 -> (s1'=2) & (u1'=(p1!=v2)) & (v1'=0) & (p1'=0);
	[read] s1=1&!u1&c=N-1 -> (s1'=2) & (u1'=false) & (v1'=0);
	[done] s1=2 -> (s1'=3) & (u1'=false) & (v1'=0) & (p1'=0);
	[retry] s1=2 -> (s1'=0) & (u1'=false) & (v1'=0) & (p1'=0);
	[loop] s1=3 -> (s1'=3);

endmodule

module process2

	s2 : [0..3];
	u2 : bool;
	v2 : [0..K-1];
	p2 : [0..K-1];

	[pick] s2=0 -> 1/K : (s2'=1) & (p2'=0) & (v2'=0) & (u2'=true) + 1/K : (s2'=1) & (p2'=1) & (v2'=1) & (u2'=true) + 1/K : (s2'=1) & (p2'=2) & (v2'=2) & (u2'=true) + 1/K : (s2'=1) & (p2'=3) & (v2'=3) & (u2'=true) + 1/K : (s2'=1) & (p2'=4) & (v2'=4) & (u2'=true) + 1/K : (s2'=1) & (p2'=5) & (v2'=5) & (u2'=true);
	[read] s2=1&u2&c<N-1 -> (u2'=(p2!=v3)) & (v2'=v3);
	[read] s2=1&!u2&c<N-1 -> (u2'=false) & (v2'=v3) & (p2'=0);
	[read] s2=1&u2&c=N-1 -> (s2'=2) & (u2'=(p2!=v3)) & (v2'=0) & (p2'=0);
	[read] s2=1&!u2&c=N-1 -> (s2'=2) & (u2'=false) & (v2'=0);
	[done] s2=2 -> (s2'=3) & (u2'=false) & (v2'=0) & (p2'=0);
	[retry] s2=2 -> (s2'=0) & (u2'=false) & (v2'=0) & (p2'=0);
	[loop] s2=3 -> (s2'=3);

endmodule

module process3

	s3 : [0..3];
	u3 : bool;
	v3 : [0..K-1];
	p3 : [0..K-1];

	[pick] s3=0 -> 1/K : (s3'=1) & (p3'=0) & (v3'=0) & (u3'=true) + 1/K : (s3'=1) & (p3'=1) & (v3'=1) & (u3'=true) + 1/K : (s3'=1) & (p3'=2) & (v3'=2) & (u3'=true) + 1/K : (s3'=1) & (p3'=3) & (v3'=3) & (u3'=true) + 1/K : (s3'=1) & (p3'=4) & (v3'=4) & (u3'=true) + 1/K : (s3'=1) & (p3'=5) & (v3'=5) & (u3'=true);
	[read] s3=1&u3&c<N-1 -> (u3'=(p3!=v4)) & (v3'=v4);
	[read] s3=1&!u3&c<N-1 -> (u3'=false) & (v3'=v4) & (p3'=0);
	[read] s3=1&u3&c=N-1 -> (s3'=2) & (u3'=(p3!=v4)) & (v3'=0) & (p3'=0);
	[read] s3=1&!u3&c=N-1 -> (s3'=2) & (u3'=false) & (v3'=0);
	[done] s3=2 -> (s3'=3) & (u3'=false) & (v3'=0) & (p3'=0);
	[retry] s3=2 -> (s3'=0) & (u3'=false) & (v3'=0) & (p3'=0);
	[loop] s3=3 -> (s3'=3);

endmodule

module process4

	s4 : [0..3];
	u4 : bool;
	v4 : [0..K-1];
	p4 : [0..K-1];

	[pick] s4=0 -> 1/K : (s4'=1) & (p4'=0) & (v4'=0) & (u4'=true) + 1/K : (s4'=1) & (p4'=1) & (v4'=1) & (u4'=true) + 1/K : (s4'=1) & (p4'=2) & (v4'=2) & (u4'=true) + 1/K : (s4'=1) & (p4'=3) & (v4'=3) & (u4'=true) + 1/K : (s4'=1) & (p4'=4) & (v4'=4) & (u4'=true) + 1/K : (s4'=1) & (p4'=5) & (v4'=5) & (u4'=true);
	[read] s4=1&u4&c<N-1 -> (u4'=(p4!=v1)) & (v4'=v1);
	[read] s4=1&!u4&c<N-1 -> (u4'=false) & (v4'=v1) & (p4'=0);
	[read] s4=1&u4&c=N-1 -> (s4'=2) & (u4'=(p4!=v1)) & (v4'=0) & (p4'=0);
	[read] s4=1&!u4&c=N-1 -> (s4'=2) & (u4'=false) & (v4'=0);
	[done] s4=2 -> (s4'=3) & (u4'=false) & (v4'=0) & (p4'=0);
	[retry] s4=2 -> (s4'=0) & (u4'=false) & (v4'=0) & (p4'=0);
	[loop] s4=3 -> (s4'=3);

endmodule

//rewards "num_rounds"

//	[pick] true : 1;

//endrewards
