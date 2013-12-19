DIR=`dirname $0`
java -cp $DIR/cobertura-2.0.3.jar:$DIR/lib/asm-4.1.jar:$DIR/lib/asm-util-4.1.jar:$DIR/lib/asm-tree-4.1.jar:$DIR/lib/asm-commons-4.1.jar:$DIR/lib/asm-analysis-4.1.jar:$DIR/lib/log4j-1.2.9.jar:$DIR/lib/oro-2.0.8.jar net.sourceforge.cobertura.instrument.InstrumentMain $*
