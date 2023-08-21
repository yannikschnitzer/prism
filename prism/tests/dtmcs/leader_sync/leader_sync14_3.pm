// synchronous leader election protocol  (itai & Rodeh)
// dxp/gxn 25/01/01

dtmc

// CONSTANTS
const N = 14; // number of processes
const K = 3; // range of probabilistic choice

// counter module used to count the number of processes that have been read
// and to know when a process has decided
module counter
	
	// counter (c=i  means process j reading process (i-1)+j next)
	c : [1..N-1];
	
	// reading
	[read] c<N-1 -> (c'=c+1);
	// finished reading
	[read] c=N-1 -> (c'=c);
	//decide
	[done] u1|u2|u3|u4|u5|u6|u7|u8|u9|u10|u11|u12|u13|u14 -> (c'=c);
	// pick again reset counter 
	[retry] !(u1|u2|u3|u4|u5|u6|u7|u8|u9|u10|u11|u12|u13|u14) -> (c'=1);
	// loop (when finished to avoid deadlocks)
	[loop] s1=3 -> (c'=c);
	
endmodule

//  processes form a ring and suppose:
// process 1 reads process 2
// process 2 reads process 3
// process 3 reads process 1
module process1
	
	// local state
	s1 : [0..3];
	// s1=0 make random choice
	// s1=1 reading
	// s1=2 deciding
	// s1=3 finished
	
	// has a unique id so far (initially true)
	u1 : bool;
	
	// value to be sent to next process in the ring (initially sets this to its own value)
	v1 : [0..K-1];
	
	// random choice
	p1 : [0..K-1];
	
	// pick value
	[pick] s1=0 -> 1/K : (s1'=1) & (p1'=0) & (v1'=0) & (u1'=true)
	             + 1/K : (s1'=1) & (p1'=1) & (v1'=1) & (u1'=true)
	             + 1/K : (s1'=1) & (p1'=2) & (v1'=2) & (u1'=true);
	// read
	[read] s1=1 &  u1 & c<N-1 -> (u1'=(p1!=v2)) & (v1'=v2);
	[read] s1=1 & !u1 & c<N-1 -> (u1'=false) & (v1'=v2) & (p1'=0);
	// read and move to decide
	[read] s1=1 &  u1 & c=N-1 -> (s1'=2) & (u1'=(p1!=v2)) & (v1'=0) & (p1'=0);
	[read] s1=1 & !u1 & c=N-1 -> (s1'=2) & (u1'=false) & (v1'=0);
	// deciding
	// done
	[done] s1=2 -> (s1'=3) & (u1'=false) & (v1'=0) & (p1'=0);
	//retry
	[retry] s1=2 -> (s1'=0) & (u1'=false) & (v1'=0) & (p1'=0);
	// loop (when finished to avoid deadlocks)
	[loop] s1=3 -> (s1'=3);
	
endmodule

// construct remaining processes through renaming
module process2 = process1 [ s1=s2,p1=p2,v1=v2,u1=u2,v2=v3 ] endmodule
module process3 = process1 [ s1=s3,p1=p3,v1=v3,u1=u3,v2=v4 ] endmodule
module process4 = process1 [ s1=s4,p1=p4,v1=v4,u1=u4,v2=v5 ] endmodule
module process5 = process1 [ s1=s5,p1=p5,v1=v5,u1=u5,v2=v6 ] endmodule
module process6 = process1 [ s1=s6,p1=p6,v1=v6,u1=u6,v2=v7 ] endmodule
module process7 = process1 [ s1=s7,p1=p7,v1=v7,u1=u7,v2=v8 ] endmodule
module process8 = process1 [ s1=s8,p1=p8,v1=v8,u1=u8,v2=v9 ] endmodule
module process9 = process1 [ s1=s9,p1=p9,v1=v9,u1=u9,v2=v10 ] endmodule
module process10 = process1 [ s1=s10,p1=p10,v1=v10,u1=u10,v2=v11 ] endmodule
module process11 = process1 [ s1=s11,p1=p11,v1=v11,u1=u11,v2=v12 ] endmodule
module process12 = process1 [ s1=s12,p1=p12,v1=v12,u1=u12,v2=v13 ] endmodule
module process13 = process1 [ s1=s13,p1=p13,v1=v13,u1=u13,v2=v14 ] endmodule
module process14 = process1 [ s1=s14,p1=p14,v1=v14,u1=u14,v2=v1 ] endmodule

// expected number of rounds
rewards "num_rounds"
	s1=0 : 1;
endrewards

// labels
label "elected" = s1=3&s2=3&s3=3&s4=3&s5=3&s6=3&s7=3&s8=3&s9=3&s10=3&s11=3&s12=3&s13=3&s14=3;

