dtmc
const int k;

module twostate
	a : [1..3];
	t : [0..k];

	[] a=1 & t<k -> 0.5 : (t'=t+1) + 0.5 : (a'=2) & (t'=t+1);
	[] a=2 & t<k -> 0.4 : (t'=t+1) + 0.5 : (a'=1) & (t'=t+1) + 0.1 : (a'=3) & (t'=t+1);
	[] a=3 & t<k -> (t'=t+1); 
endmodule