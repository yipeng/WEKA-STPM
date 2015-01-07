WEKA-SPTM
=========

WEKA for Moving Object Data Analysis and Mining (Weka-STPM)

This github project is a port of Weka-STPM that takes care of some bugbears in the original code base. It is intended to provide compatibility with Postgis 2.X but I think my changes fix compatibility with >1.3. 

There are two copies of Wekas-STPM provided by the authors:
* http://www.inf.ufsc.br/~vania/software.html
* http://www.inf.ufrgs.br/~alvares/software.html

I use Alvares's copy as the base. It is newer but both codebases is largely the same. There is an attempt to generalize the hard-coded parameters i.e. table names, parameter names into a configuration but at first glance this seems to be accessable only by source. I also observe an addtional package on trajectory cleaning.



