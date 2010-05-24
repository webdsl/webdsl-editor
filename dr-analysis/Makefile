XTC=--xtc-repo /home/nathan/stratego-xt/BUILDTIME_XTC
STRC=strc
WEBDSL_HOME=~/webdsl-java/src
NIX_PROFILE=~/.nix-profile

all : ../include/webdsl-typechecker-rules.aterm

../include/webdsl-typechecker-rules.aterm : bin/webdsl-generator-packed.str dranalysis
bin/webdsl-generator-packed.str : packstratego
	./packstratego -i $(WEBDSL_HOME)/org/webdsl/dsl/generation/webdsl-generator.str -I $(WEBDSL_HOME) -I $(WEBDSL_HOME)/org/webdsl/dsl/syntax -I $(NIX_PROFILE)/share/java-front -o $@

% : %.str
	$(STRC) -i $< -o $@ -Cl stratego-lib -Cl strc $(XTC)

#dranalysis : dranalysis.str
#	strc -i dr-analysis.str -o dr-analysis -Cl stratego-lib -Cl strc -Cl stratego-aterm $(XTC)

#test : dr-analysis
#	./dr-analysis -i ~/workspace/analyze/webdsl-generator-packed.str

