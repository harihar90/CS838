
public class Test {
	public static void main(String args[])
	{
		String word = "#ass12-12,!s:'sa";
		word = word.replaceAll("[^a-zA-Z0-9\\-]", "");
		System.out.println(word);
	}
}
