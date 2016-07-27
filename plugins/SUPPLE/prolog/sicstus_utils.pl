
/* ------------------------------------------------------------------------
 > FILENAME:	sicstus_utils
 > PURPOSE:	
 > AUTHORS:	Kevin Humphreys
 > NOTES:	
 ------------------------------------------------------------------------ */

cvsid_sicstus_utils("$Id: sicstus_utils.pl 7085 2005-12-05 16:32:03Z ian_roberts $").


:- compile('supple_utils.pl').

:- use_module(library(lists)). % member/2, memberchk/2, non_member/2, append/3
                            % no_doubles/2, last/2, nth/3, nth0/3, reverse/2

% quintus compatibility:

:- op(700, xfx, \=). 
X \= Y :- \+(X=Y).
not(Goal) :- \+(Goal).

:- op(900, fy, once).
once(Goal) :- call(Goal), !.

%   lower(+Text, ?Lower)
lower(Text, Lower) :-
	atom(Text),
	atom_chars(Text, TextChars),
	lower_chars(TextChars, LowerChars),
	atom_chars(Lower, LowerChars).