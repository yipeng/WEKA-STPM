WEKA-SPTM
=========

WEKA for Moving Object Data Analysis and Mining (Weka-STPM)

Weka-STPM extends "Weka to support automatic trajectory data preprocessing to add semantic information to raw trajectory data for analysis and mining. This version implements the method IB-SMOT (ACMGIS 2007) and the method CB-SMOT (ACMSAC 2008)."  Stop and Move of Trajectories (SMOT) algorithms are pretty neat because they actually work :)
This github project is a port of the original Weka-STPM that takes care of some bugbears in the codebase. It is intended to provide compatibility with Postgis 2.X but I think my changes fix compatibility with >1.3. 

There are two copies of Wekas-STPM provided by the authors:
* http://www.inf.ufsc.br/~vania/software.html
* http://www.inf.ufrgs.br/~alvares/software.html

I use Alvares's copy as the base. It is newer but both codebases is largely the same. There is an attempt to generalize the hard-coded parameters i.e. table names, parameter names into a configuration but at first glance this seems to be accessable only by source. I also observe an addtional package on trajectory cleaning.
	


