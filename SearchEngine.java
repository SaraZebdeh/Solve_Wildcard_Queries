
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SearchEngine {

    public static Map<String, String> permutermIndex = new HashMap<>();

    public static String generatePermutermForWildcardQuery(String query) {
        String queryWithMarker = query + "$";
        String rotatedQuery = null;
        for (int i = 0; i < queryWithMarker.length(); i++) {
            rotatedQuery = queryWithMarker.substring(queryWithMarker.length() - 1 - i) +
                           queryWithMarker.substring(0, queryWithMarker.length() - 1 - i);
            if (rotatedQuery.charAt(rotatedQuery.length() - 1) == '*') {
                break;
            }
        }
        return rotatedQuery;
    }

    public static String stem(String word) {
        // Apply the Porter Stemming Algorithm rules

        if (word.endsWith("sses") || word.endsWith("ies")) {
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ss")) {
            //do nothing
        } else if (word.endsWith("s") && word.length() > 2) {
            word = word.substring(0, word.length() - 1);
        }

        if (word.endsWith("ing")) {
            word = word.substring(0, word.length() - 3);
            if (word.endsWith("at") || word.endsWith("bl") || word.endsWith("iz")) {
                word = word.concat("e");
            }
            
        } else if (word.endsWith("ed")) {
            word = word.substring(0, word.length() - 2);
            if ((word.endsWith("at") || word.endsWith("bl") || word.endsWith("iz")) && word.length() > 2) {
                word = word.concat("e");
            }
        } else if (word.endsWith("eed")) {
            word = word.substring(0, word.length() - 3);
            if ((word.endsWith("at") || word.endsWith("bl") || word.endsWith("iz")) && word.length() > 2) {
                word = word.concat("e");
            }
        }
        
        if (word.endsWith("ied") || word.endsWith("ies")) {
            word = word.substring(0, word.length() - 2) + "i";
        }

        if (word.endsWith("y") && word.length() > 2 && !isVowel(word.charAt(word.length() - 2))) {
            word = word.substring(0, word.length() - 1) + "i";
        }

        return word;
    }

    private static boolean isVowel(char c) {
        return "aeiouAEIOU".contains(String.valueOf(c));
    }

    private static List<ContentObjectToWords> stopWords(List<ContentObjectToWords> data) {
        List<ContentObjectToWords> removeStopWords = data;

        for (int i = 0; i < removeStopWords.size(); i++) {
            List<String> content = removeStopWords.get(i).getContent();
            for (int j = 0; j < content.size(); j++) {
                if (content.get(j).equalsIgnoreCase("is")  || content.get(j).equalsIgnoreCase("an")  ||
                    content.get(j).equalsIgnoreCase("and") || content.get(j).equalsIgnoreCase("are") || 
                    content.get(j).equalsIgnoreCase("a")   || content.get(j).equalsIgnoreCase("as")  ||
                    content.get(j).equalsIgnoreCase("at")  || content.get(j).equalsIgnoreCase("for") || 
                    content.get(j).equalsIgnoreCase("from")|| content.get(j).equalsIgnoreCase("in")  ||
                    content.get(j).equalsIgnoreCase("is")  || content.get(j).equalsIgnoreCase("it")  ||
                    content.get(j).equalsIgnoreCase("of")  || content.get(j).equalsIgnoreCase("on")  || 
                    content.get(j).equalsIgnoreCase("or")  || content.get(j).equalsIgnoreCase("the") ||
                    content.get(j).equalsIgnoreCase("to")){
                    content.remove(j);
                    j--;
                }
            }
            removeStopWords.get(i).setContent(content);
        }

        return removeStopWords;
    }

    public static List<String> Permuterm(List<String> terms, String permutermToSearch) {
        int count = 0, index1 = 0;
        for (int i = 0; i < permutermToSearch.length(); i++) {
            if (permutermToSearch.charAt(i) == '*') {
                count++;
                if (count == 1) {
                    index1 = i;
                } 
            }
        }

        List<String> matchingValues = new ArrayList<>();
        for (int i = 0; i < terms.size(); i++) {
            generatePermutermIndex(terms.get(i));
        }

        Set<Entry<String, String>> permutermsArray;
        permutermsArray = permutermIndex.entrySet();
//        System.out.println(permutermsArray);
        for (Entry<String, String> entry : permutermsArray) {
            String key = entry.getKey();
            if (count == 1) {
                if (key.startsWith(permutermToSearch.substring(0, permutermToSearch.length() - 1))) {
                    matchingValues.add(permutermIndex.get(key));
                }
            } else if (count == 2) {
                if (key.startsWith(permutermToSearch.substring(0, index1)) && 
                    key.contains(permutermToSearch.substring(index1 + 1, permutermToSearch.length() - 1))) {
                    matchingValues.add(permutermIndex.get(key));
                }
            }
        }
        return matchingValues;
    }

    private static List<String> rotate(String term) {
        List<String> rotations = new ArrayList<>();
        for (int i = 0; i < term.length(); i++) {
            rotations.add(term.substring(i) + term.substring(0, i));
        }
        return rotations;
    }

    static List<ContentObject> Normalization(List<ContentObject> inputList) {
        List<ContentObject> resultList = new ArrayList<>();
        for (ContentObject contentObject : inputList) {
            String contentWithoutPunctuation = contentObject.getContent().replaceAll("\\p{Punct}", "");
            resultList.add(new ContentObject(contentWithoutPunctuation, contentObject.getId()));
        }
        return resultList;
    }

    static List<ContentObjectToWords> Tokenization(List<ContentObject> inputList) {
        List<ContentObjectToWords> resultList = new ArrayList<>();
        for (ContentObject contentObject : inputList) {
            String[] words = contentObject.getContent().split("\\s+|\\p{Punct}");
            Set<String> uniqueWords = new HashSet<>();
            for (String word : words) {
                if (!word.isEmpty()) {
                    uniqueWords.add(word.toLowerCase()); // Convert to lowercase to avoid duplicates
                }
            }
            resultList.add(new ContentObjectToWords(new ArrayList<>(uniqueWords), contentObject.getId()));
        }
        return resultList;
    }

    private static void generatePermutermIndex(String term) {
        List<String> rotations = rotate(term + "$");
        for (String rotation : rotations) {
            permutermIndex.put(rotation, term);
        }
//        System.out.println(permutermIndex.entrySet().toString());
    }

    private static Map<String, List<Integer>> invertedIndex(List<ContentObjectToWords> data) {
        Map<String, List<Integer>> invertedIndex = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            List<Integer> indexes = new ArrayList<Integer>();
            List<String> content = data.get(i).getContent();
            for (int j = 0; j < content.size(); j++) {
                if (!invertedIndex.containsKey(content.get(j))) {
                    invertedIndex.put(content.get(j), new ArrayList<Integer>());
                }
                indexes = invertedIndex.get(content.get(j));
                indexes.add(data.get(i).getId());
//                System.out.println(invertedIndex.get(content.get(j)).toString());
            }
        }
        // Sort the lists of integers for each key
        for (List<Integer> indexes : invertedIndex.values()) {
            Collections.sort(indexes);
        }

        return invertedIndex;
    }

    public static void main(String[] args) {

        String wildcardQuery;
        
        String isStemming, isWithStopwords;

        
        List<ContentObject> dataset = new ArrayList<>();
        
        dataset.add(new ContentObject("Life is a beautiful journey Life, full of twists, turns and unexpected surprise, life.", 1));
        
        dataset.add(new ContentObject("Amidst (the chaos) of life moon life, find your \"inner\", calm and life Turn security.", 2));
        
        dataset.add(new ContentObject("Information retrieval is the process of obtaining *relevant information from a large collection of data or documents.", 3));
        
        dataset.add(new ContentObject("Search engines like Google utilize complex algorithms to enhance information retrieval for users worldwide FeedbAck.", 4));
        
        dataset.add(new ContentObject("The effectiveness of information retrieval systems is often measured by metrics like precision and recall.", 5));
        
        dataset.add(new ContentObject("Keyword-based searching is a common method in information retrieval, where users input specific terms to retrieve relevant documents.", 6));
        
        dataset.add(new ContentObject("Natural language processing has significantly improved the accuracy of information retrieval by understanding context and semantics.", 7));
        dataset.add(new ContentObject("Information retrieval has applications in various fields, including academia, business, healthcare, and law enforcement.", 8));
        dataset.add(new ContentObject("The advent of machine learning has enabled the development of personalized information retrieval, tailoring results to individual preferences.", 9));
        dataset.add(new ContentObject("The relevance feedback loop, where users provide input on retrieved results, helps refine information retrieval algorithms over time.", 10));
        dataset.add(new ContentObject("Challenges in information retrieval include dealing with unstructured data, ensuring privacy, and handling multilingual content.", 11));
        dataset.add(new ContentObject("As digital information continues to grow, advancements in information retrieval continue to play a crucial role in managing and accessing this vast amount of data.", 12));
        List<ContentObject> normalizedDataset = Normalization(dataset);
        for (ContentObject object : dataset) {
//            System.out.println("ID: " + object.getId() + ", Content: " + object.getContent());
//            System.out.println();
        }
        // Print the normalized content
        for (ContentObject object : normalizedDataset) {
//            System.out.println("ID: " + object.getId() + ", Content: " + object.getContent());
//            System.out.println();
        }

        // Print the tokenized content
        
        List<ContentObjectToWords> updatedListConvertContentToWords = Tokenization(dataset);
        
        for (ContentObjectToWords object : updatedListConvertContentToWords) {
//            System.out.println("ID: " + object.getId() + ", Content: " + object.getContent());
//            System.out.println();
        }

        try ( Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a wildcard query: ");
            wildcardQuery = scanner.nextLine();
            System.out.print("Do you want to do stemming for words in documents? [yes or no]: ");
            isStemming = scanner.nextLine();
            System.out.print("Do you want to do delete stop words? [yes or no]: ");
            isWithStopwords = scanner.nextLine();
        }

        // stemming
        if (isStemming.equalsIgnoreCase("yes")) {
            for (int i = 0; i < updatedListConvertContentToWords.size(); i++) {
                List<String> content = updatedListConvertContentToWords.get(i).getContent();
                for (int j = 0; j < content.size(); j++) {
                    content.set(j, stem(content.get(j)));
                }
                updatedListConvertContentToWords.get(i).setContent(content);
            }
        }

        for (ContentObjectToWords contentObject : updatedListConvertContentToWords) {
//            System.out.println("ID: " + contentObject.getId() + ", Content: " + contentObject.getContent());
//            System.out.println();
        }

        
        //stop words
        List<ContentObjectToWords> dataWithoutStopwords = null;

        if (isWithStopwords.equalsIgnoreCase("yes")) {
            dataWithoutStopwords = stopWords(updatedListConvertContentToWords);
        } else {
            dataWithoutStopwords = updatedListConvertContentToWords;
        }
        for (ContentObjectToWords contentObject : dataWithoutStopwords) {
//            System.out.println("ID: " + contentObject.getId() + ", Content: " + contentObject.getContent());
//            System.out.println();
        }

        //construct inverted index
        Map<String, List<Integer>> invertedIndex = new HashMap<>(); // Map to store document IDs for each term
        invertedIndex = invertedIndex(updatedListConvertContentToWords);
//        System.out.println(invertedIndex.toString());

        List<String> terms = new ArrayList<String>();
        terms.addAll(invertedIndex.keySet());
//        System.out.println(terms);

        
        //permuterm index query for the wild card query 
        String permuterm = null;
        permuterm = generatePermutermForWildcardQuery(wildcardQuery);

        System.out.println();
        System.out.println("Permuterm index for wildcard query " + wildcardQuery + ":");
        System.out.println(permuterm);
        System.out.println();

        List<String> matchingTerms = new ArrayList<String>();
        matchingTerms = Permuterm(terms, permuterm);
        System.out.println("Matching Terms: " + matchingTerms);
        System.out.println();
     
        List<Integer> answerIds = new ArrayList<>();
        for (int i = 0; i < matchingTerms.size(); i++) {
            answerIds.addAll(invertedIndex.get(matchingTerms.get(i)));
        }
        
        List<Integer> uniqueIds = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();

        for (Integer id : answerIds) {
            if (!seen.contains(id)) {
                seen.add(id);
                uniqueIds.add(id);
            }
        }
//        System.out.println(answerIds.toString());
//        System.out.println(uniqueIds.toString());

        for (int i = 0; i < uniqueIds.size(); i++) {
            for (int j = 0; j < dataset.size(); j++) {
                if (answerIds.get(i) == dataset.get(j).getId()) {
                    System.out.println("Document " + dataset.get(j).getId() + " : " + dataset.get(j).getContent());
                    break;
                }
            }
        }
    }
}

class ContentObject {

    private String content;
    private int id;

    public ContentObject(String content, int id) {
        this.content = content;
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public int getId() {
        return id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(int id) {
        this.id = id;
    }

}

class ContentObjectToWords {

    private List<String> content;
    private int id;

    public ContentObjectToWords(List<String> content, int id) {
        this.content = content;
        this.id = id;
    }

    public List<String> getContent() {
        return content;
    }

    public int getId() {
        return id;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public void setId(int id) {
        this.id = id;
    }

}
