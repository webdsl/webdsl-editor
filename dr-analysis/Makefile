# 

XTC=--xtc-repo /home/nathan/stratego-xt/BUILDTIME_XTC

strat : strat.str
	strc -i strat.str -o strat -Cl stratego-lib -Cl strc $(XTC)

dranalysis : dranalysis.str
	strc -i dr-analysis.str -o dr-analysis -Cl stratego-lib -Cl strc -Cl stratego-aterm $(XTC)

test : dr-analysis
	./dr-analysis -i ~/workspace/analyze/webdsl-generator-packed.str

