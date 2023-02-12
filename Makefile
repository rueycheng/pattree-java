#--------------------------------------------------
# Path to programs
#-------------------------------------------------- 
JC = /usr/bin/javac
JAR = /usr/bin/jar
PERL = /usr/bin/perl
CTAGS = /usr/bin/ctags

CLASSPATH = $(shell find lib -name '*.jar' 2>/dev/null | $(PERL) -e'print join(":", "src", map { chomp; $$_ } <>), "\n"')
SRC = $(shell find src -name '*.java')

JFLAGS = -Xlint:unchecked -cp $(CLASSPATH)
BUILD = build
BUNDLE = pattree-java-0.1.jar

#--------------------------------------------------
# Rules
#-------------------------------------------------- 
.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

.PHONY: classes jar clean all

all: jar

jar: $(BUNDLE)

$(BUNDLE): $(SRC:.java=.class)
	(cd src; find -name '*.class' -print0 | xargs -0 $(JAR) cvM) > $@

#--------------------------------------------------
# Tags support are optional
#-------------------------------------------------- 
tags:
	$(CTAGS) -R src

clean:
	find src -name '*.class' -delete 
