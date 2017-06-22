import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.*;
import java.util.*;
import com.opencsv.CSVReader;

public class ExcelToFile extends JFrame {
	public static ExcelToFile frame;
	public static JFrame frameFileChooser;
	public JFileChooser fc;
	public File fileToOpen;
	public File fileToSave;
	public CSVReader reader = null;
	public FileReader fileReader;
	List<String[]> competitions;
	List<String[]> conferences;

	private static final int TIMESTAMP_INDEX = 0;
	private static final int JENIS_LOMBA_INDEX = 1;
	private static final int NAMA_LOMBA_INDEX = 2;
	private static final int PENYELENGGARA_INDEX = 3;
	private static final int TINGKAT_INDEX = 4;
	private static final int TEMPAT_INDEX = 5;
	private static final int TEMA_INDEX = 6;
	private static final int AWAL_REGIS_INDEX = 7;
	private static final int AKHIR_REGIS_INDEX = 8;
	private static final int DEADLINE_INDEX = 9;
	private static final int TANGGAL_ACARA_INDEX = 10;
	private static final int WEBSITE_INDEX = 11;
	private static final int CONTACT_PERSON_INDEX = 12;
	private static final int SOCIAL_MEDIA_INDEX = 13;
	private static final int NOTES_INDEX = 14;

	Map<Integer, String> mapColumnToName;

	Comparator comparator = new Comparator<String[]>() {
										    @Override
										    public int compare(String[] o1, String[] o2) {
										    	Date d1 = parseAkhirRegis(o1, "MM/dd/yyyy");
										    	Date d2 = parseAkhirRegis(o2, "MM/dd/yyyy");
										    	if (d1 != null && d2 != null)
											    	return d1.compareTo(d2);
											    else
											    	return 0;
										    }
										};

	public ExcelToFile(String frameName) {
		super(frameName);
		competitions = new ArrayList<String[]>();
		conferences = new ArrayList<String[]>();
		mapColumnToName = new HashMap<Integer, String>();
	}

	public static void main(String[] args) {
		initFrame();
		frame.initFrameComponents();
		frame.openFile("");
		frame.readAndClassifyPerLine();
		frame.processCompetitions();
		frame.processConferences();
	}

	public static void initFrame() {
		frame = new ExcelToFile("Excel to File");
		frameFileChooser = new JFrame("Choose file");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
 	public void initFrameComponents() {
		JPanel panelTop = new JPanel();
		JPanel panelChooseFile = new JPanel();

		JButton buttonSubmit = new JButton("Convert");
		buttonSubmit.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					frameFileChooser.add(fc);
					frameFileChooser.pack();
					frameFileChooser.setVisible(true);
				}
			}
		);
		fc = new JFileChooser();
		panelTop.add(fc);
		panelTop.add(buttonSubmit);
		frame.add(panelTop);
	}

	public void openFile(String fileAbsPath){
		try {
			fileReader = new FileReader("C:\\Users\\zxcma\\OneDrive\\Documents\\Projects\\file.csv");
			reader = new CSVReader(fileReader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void readAndClassifyPerLine() {
		if (reader != null) {
			String[] stringByColumn = null;
			boolean isFirstLine = true;
			try {
				while ((stringByColumn = reader.readNext()) != null) {
					if (isFirstLine) {
						isFirstLine = false;
						initiateMapToColumnName(stringByColumn);
						continue;
					}
					boolean haventPassed = true;
					if (!isNullOrEmpty(stringByColumn[AKHIR_REGIS_INDEX])) {
						
					} else if (!isNullOrEmpty(stringByColumn[DEADLINE_INDEX])) {
						
					}
					if (haventPassed) {
						classifyEvent(stringByColumn);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			//pop up error please choose fileb
		}
	}

	public void initiateMapToColumnName(String[] columns) {
		for (int i = 0; i < columns.length; i++) {
			mapColumnToName.put(i, columns[i]);
		}
	}

	public void classifyEvent(String[] columns) {
		if (columns[JENIS_LOMBA_INDEX].equalsIgnoreCase("Konferensi")) {
			conferences.add(columns);
		} else {
			competitions.add(columns);
		}
	}

	public void processCompetitions() {
		Collections.sort(competitions, comparator);
		System.out.println("COMPETITION:");
		for (int i = 0; i < competitions.size(); i++) {
			String result = parseEvent(i+1, competitions.get(i));
			System.out.println(result);
		}
	}

	public void processConferences() {
		Collections.sort(conferences, comparator);
		System.out.println("CONFERENCE:");
		for (int i = 0; i < conferences.size(); i++) {
			System.out.println(parseEvent(i+1, conferences.get(i)));
		}
	}

	private String formatToFullDate(String content) {
		String result = null;
		try {
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			Date date = df.parse(content);
			DateFormat df2 = new SimpleDateFormat("dd MMMM yyyy");
			result = df2.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	private boolean isNullOrEmpty(String content) {
		return content == null || content.isEmpty();
	}


	private String parseEvent(int i , String[] content) {
		StringBuilder builder = new StringBuilder();
		builder.append(i);
		builder.append(". ");


		//nama lomba
		if (!isNullOrEmpty(content[NAMA_LOMBA_INDEX])) {
			builder.append(mapColumnToName.get(NAMA_LOMBA_INDEX));
			builder.append(": ");
			builder.append(content[NAMA_LOMBA_INDEX]+"\n");
		}

		//tema lomba
		if (!isNullOrEmpty(content[TEMA_INDEX])) {
			builder.append(mapColumnToName.get(TEMA_INDEX));
			builder.append(": ");
			builder.append(content[TEMA_INDEX]+"\n");
		}

		//penyelenggara
		if (!isNullOrEmpty(content[PENYELENGGARA_INDEX])) {
			builder.append(mapColumnToName.get(PENYELENGGARA_INDEX));
			builder.append(": ");
			builder.append(content[PENYELENGGARA_INDEX]+"\n");
		}

		//Akhir Registrasi
		if (!isNullOrEmpty(content[AKHIR_REGIS_INDEX])) {
			builder.append(mapColumnToName.get(AKHIR_REGIS_INDEX));
			builder.append(": ");
			builder.append(formatToFullDate(content[AKHIR_REGIS_INDEX])+"\n");
		} else if (!isNullOrEmpty(content[DEADLINE_INDEX])) {
			builder.append(mapColumnToName.get(DEADLINE_INDEX));
			builder.append(": ");
			builder.append(formatToFullDate(content[DEADLINE_INDEX])+"\n");
		}

		//Tanggal Acara
		if (!isNullOrEmpty(content[TANGGAL_ACARA_INDEX])) {
			builder.append(mapColumnToName.get(TANGGAL_ACARA_INDEX));
			builder.append(": ");
			builder.append(content[TANGGAL_ACARA_INDEX]+"\n");
		}

		//website
		if (!isNullOrEmpty(content[WEBSITE_INDEX])) {
			builder.append(mapColumnToName.get(WEBSITE_INDEX));
			builder.append(": ");
			builder.append(content[WEBSITE_INDEX]+"\n");
		}

		//email
		if (!isNullOrEmpty(content[CONTACT_PERSON_INDEX])) {
			builder.append(mapColumnToName.get(CONTACT_PERSON_INDEX));
			builder.append(": ");
			builder.append(content[CONTACT_PERSON_INDEX]+"\n");
		}

		return builder.toString();
	}



	public Date parseAkhirRegis(String[] obj, String formatDate) {
		DateFormat df = new SimpleDateFormat(formatDate);
    	Date d1 = null;
    	try {
    		d1 = df.parse(obj[AKHIR_REGIS_INDEX]);
    	} catch (ParseException e) {
    		try {
    			d1 = df.parse(obj[DEADLINE_INDEX]);
    		} catch (ParseException e2) {

    		}
    	}
    	return d1;
	}
}