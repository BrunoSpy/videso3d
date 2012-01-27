/*
 * This file is part of ViDESO.
 * ViDESO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ViDESO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ViDESO.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.crnan.videso3d.util.diff;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;


/**
@see bmsi.util.Diff
@author Stuart D. Gathman
* Copyright (C) 2000 Business Management Systems, Inc.
*/
public abstract class AbstractDiffPrinter {
	
	protected PrintWriter outfile;
	
	public void setOutput(Writer wtr) {
		outfile = new PrintWriter(wtr);
	}
	protected void setupOutput() {
		if (outfile == null)
			outfile = new PrintWriter(new OutputStreamWriter(System.out));
	}
	public AbstractDiffPrinter(Object[] a,Object[] b) {
		file0 = a;
		file1 = b;
	}
	/** Set to ignore certain kinds of lines when printing
an edit script.  For example, ignoring blank lines or comments.
	 */
	protected UnaryPredicate ignore = null;

	/** Set to the lines of the files being compared.
	 */
	protected Object[] file0, file1;

	/** Divide SCRIPT into pieces by calling HUNKFUN and
   print each piece with PRINTFUN.
   Both functions take one arg, an edit script.

   PRINTFUN takes a subscript which belongs together (with a null
   link at the end) and prints it.  */
	public void print_script(Diff.change script) {
		setupOutput();
		Diff.change next = script;

		while (next != null)
		{
			Diff.change t, end;

			/* Find a set of changes that belong together.  */
			t = next;
			end = hunkfun(next);

			/* Disconnect them from the rest of the changes,
     making them a hunk, and remember the rest for next iteration.  */
			next = end.link;
			end.link = null;
			//if (DEBUG)
			//  debug_script(t);

			/* Print this hunk.  */
			print_hunk(t);

			/* Reconnect the script so it will all be freed properly.  */
			end.link = next;
		}
		outfile.flush();
	}

	/** Called with the tail of the script
   and returns the last link that belongs together with the start
   of the tail. */

	protected Diff.change hunkfun(Diff.change hunk) {
		return hunk;
	}

	protected int first0, last0, first1, last1, deletes, inserts;

	/** Look at a hunk of edit script and report the range of lines in each file
  that it applies to.  HUNK is the start of the hunk, which is a chain
  of `struct change'.  The first and last line numbers of file 0 are stored
  in *FIRST0 and *LAST0, and likewise for file 1 in *FIRST1 and *LAST1. 
  Note that these are internal line numbers that count from 0.

  If no lines from file 0 are deleted, then FIRST0 is LAST0+1.

  Also set *DELETES nonzero if any lines of file 0 are deleted
  and set *INSERTS nonzero if any lines of file 1 are inserted.
  If only ignorable lines are inserted or deleted, both are
  set to 0.  */

	protected void analyze_hunk(Diff.change hunk) {
		int f0, l0 = 0, f1, l1 = 0, show_from = 0, show_to = 0;
		int i;
		Diff.change next;
		boolean nontrivial = (ignore == null);

		show_from = show_to = 0;

		f0 = hunk.line0;
		f1 = hunk.line1;

		for (next = hunk; next != null; next = next.link)
		{
			l0 = next.line0 + next.deleted - 1;
			l1 = next.line1 + next.inserted - 1;
			show_from += next.deleted;
			show_to += next.inserted;
			for (i = next.line0; i <= l0 && ! nontrivial; i++)
				if (!ignore.execute(file0[i]))
					nontrivial = true;
			for (i = next.line1; i <= l1 && ! nontrivial; i++)
				if (!ignore.execute(file1[i]))
					nontrivial = true;
		}

		first0 = f0;
		last0 = l0;
		first1 = f1;
		last1 = l1;

		/* If all inserted or deleted lines are ignorable,
 tell the caller to ignore this hunk.  */

		if (!nontrivial)
			show_from = show_to = 0;

		deletes = show_from;
		inserts = show_to;
	}

	/** Called to print the script header which identifies the files compared.
  The default does nothing (except set output to system.out if
  not otherwise set).  Derived style classes can override to print 
  the files compared in the format for that style.
	 */
	public void print_header(String filea, String fileb) {
		setupOutput();
	}

	protected abstract void print_hunk(Diff.change hunk);

	protected void print_1_line(String pre,Object linbuf) {
		outfile.println(pre + linbuf.toString());
	}

	/** Print a pair of line numbers with SEPCHAR, translated for file FILE.
   If the two numbers are identical, print just one number.

   Args A and B are internal line numbers.
   We print the translated (real) line numbers.  */

	protected void print_number_range (char sepchar, int a, int b) {
		/* Note: we can have B < A in the case of a range of no lines.
 In this case, we should print the line number before the range,
 which is B.  */
		if (++b > ++a)
			outfile.print("" + a + sepchar + b);
		else
			outfile.print(b);
	}

	public static char change_letter(int inserts, int deletes) {
		if (inserts == 0)
			return 'd';
		else if (deletes == 0)
			return 'a';
		else
			return 'c';
	}
}

