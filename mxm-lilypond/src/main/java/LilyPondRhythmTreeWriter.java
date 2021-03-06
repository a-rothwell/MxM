import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

/*
public class LilyPondRhythmTreeWriter extends IFileWriter<RhythmTree> {

    @Override
    public void write(RhythmTree rhythmTree, String filename) {
        try {
            PrintWriter writer = new PrintWriter(filename + ".ly", "UTF-8");
            writer.print(getHeader("Rhythm Tree","s"));
            writer.print(getRhythmTreeString(rhythmTree,"Rhythm Tree"));
            writer.close();
        }
        catch (IOException e) {
            // do something
        }
    }

    @Override
    public void write(Collection<RhythmTree> rhythmTrees, String filename) {
        try {
            PrintWriter writer = new PrintWriter(filename + ".ly", "UTF-8");
            writer.print(getHeader("Rhythm Trees",rhythmTrees.size()+" Total"));
            int count = 0;
            for(RhythmTree rhythmTree : rhythmTrees) {
                writer.print(getRhythmTreeString(rhythmTree,"Rhythm Tree #"+(++count)));
            }
            writer.close();
        }
        catch (IOException e) {
            // do something
        }
    }

    public static String getHeader(String title, String subtitle) {
        return  "\\version \"2.18.2\"\n" +
                "\\header {\n" +
                "\ttitle = \"" + title + "\"\n" +
                "\tsubtitle = \"" + subtitle + "\"\n" +
                "\tcomposer = \"Music ex Machina\"\n" +
                "\ttagline = \"This piece was generated by io.MxmLog and engraved by LilyPond.\"\n" +
                "\tcopyright = \"All Rights Reserved\"\n"+
                "}\n";
    }

    public static String getRhythmTreeString(RhythmTree rhythmTree, String treeName){
        return "\\score { \n" +
                "\t\\new Staff {\n" +
                "\t\t"+getNoteString(rhythmTree.getRoot(),0, TimeSig.get(4,4)) + "\n" +
                "\t}\n" +
                "\t\\header {\n" +
                "\t\tpiece = \"" + treeName + "\"\n" +
                "\t}\n" +
                "}";
    }

    public static String getNoteString(RhythmNode node, int level, TimeSig timeSignature) {
        String toReturn = "";

        // If this is a leaf node
        if(node.getValue() == 0) {
            Count nodeSize = timeSignature.getMeasureSize();
            nodeSize = timeSignature.getPreferredNoteLength(level);

            if(nodeSize.getNumerator() == 1) {
                toReturn += " c'"+timeSignature.getPreferredNoteLength(level).getDenominator();
            }
            else if(nodeSize.getNumerator() == 3) {
                toReturn += " c'"+timeSignature.getPreferredNoteLength(level).getDenominator()/2+".";
            }
            else System.out.println("!");
        }
        else {
            int preferredSubdivision = timeSignature.getPreferredSubdivision(level);
            int actualSubdivision    = node.getValue();

            // Tuplets
            if(preferredSubdivision % actualSubdivision != 0) {
                // Think about 5 notes in 4/4... you want that as five QUARTERS, not halves
                while(actualSubdivision/preferredSubdivision > 1) {
                    preferredSubdivision = preferredSubdivision*2;
                    level += 1;
                }

                toReturn += " \\tuplet " + actualSubdivision + "/" + preferredSubdivision + " {";
                for(RhythmNode child : node.getChildren()) {
                    toReturn += getNoteString(child,level+1,timeSignature);
                }
                toReturn += "}";
            }
            else {
                for(RhythmNode child : node.getChildren()) {
                    toReturn += getNoteString(child,level+1,timeSignature);
                }
            }
        }
        return toReturn;
    }

    public static void main(String args[]) {
        ArrayList<RhythmTree> trees = new ArrayList<>();
        trees.add(new RhythmTree(new int[]{2,0,0}));
        trees.add(new RhythmTree(new int[]{3,0,0,0}));
        trees.add(new RhythmTree(new int[]{2,2,2,0,0,0,0}));
        trees.add(new RhythmTree(new int[]{5,0,0,0,0,0}));
        trees.add(new RhythmTree(new int[]{2,3,3,0,0,0,0,0,0}));
        trees.add(new RhythmTree(new int[]{3,2,2,2,0,0,0,0,0,0}));
        trees.add(new RhythmTree(new int[]{2,0,2,0,2,0,2,0,1}));
        IFileWriter writer = new LilyPondRhythmTreeWriter();

        writer.write(trees,"test");
        LilyPondTools.engrave("test.ly");
    }

}
*/