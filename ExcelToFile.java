import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.filechooser.FileNameExtensionFilter;
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
	public FileWriter fileWriter;
	public BufferedWriter bufferedWriter;
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

	String absPath, parentPath;

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
	}

	public static void initFrame() {
		frame = new ExcelToFile("Excel to File");
		frame.setMinimumSize(new Dimension(500,300));
		frameFileChooser = new JFrame("Choose file");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
 	public void initFrameComponents() {
 		JPanel parentPanel = new JPanel();
 		GridLayout gridLayoutParent = new GridLayout(2,1);
 		parentPanel.setLayout(gridLayoutParent);
		JPanel panelTop = new JPanel();
		GridLayout gridLayout = new GridLayout(1,2);
		panelTop.setMaximumSize(new Dimension(500,100));
		panelTop.setLayout(gridLayout);
		JPanel panelBottom = new JPanel();
		JButton buttonSubmit = new JButton("Convert");
		JButton buttonChooseFile = new JButton("Choose File");
		JTextField textFieldPath = new JTextField();
		textFieldPath.setMaximumSize(new Dimension(300,25));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files", "csv");
		fc = new JFileChooser();
		fc.setFileFilter(filter);
		buttonChooseFile.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int returnVal = fc.showOpenDialog(ExcelToFile.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						absPath = fc.getSelectedFile().getAbsolutePath();
						textFieldPath.setText(absPath);
					}
				}
			}
		);

		buttonSubmit.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					processFile(absPath);
				}
			}
		);


		GroupLayout layout =  new GroupLayout(parentPanel);
		parentPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(
			layout.createSequentialGroup()
					.addComponent(buttonChooseFile)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(textFieldPath).addComponent(buttonSubmit))
		);

		layout.setVerticalGroup(
			layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
					addComponent(buttonChooseFile).addComponent(textFieldPath))
			.addComponent(buttonSubmit));
		frame.add(parentPanel);
		frame.pack();	
	}

	public void processFile(String absPath) {
		openFile(absPath);
		readAndClassifyPerLine();
		processCompetitions();
		processConferences();
		close();
	}

	public void openFile(String fileAbsPath){
		try {
			File file = new File(absPath);
			parentPath = file.getParent();
			fileReader = new FileReader(absPath);
			reader = new CSVReader(fileReader);
			fileWriter = new FileWriter(parentPath+"\\output.txt", true);
			bufferedWriter = new BufferedWriter(fileWriter);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
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
					if (isEventPassed(stringByColumn)) {
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
		if (competitions.size() == 0) {
			return;
		}
		Collections.sort(competitions, comparator);
		println("COMPETITION:");
		for (int i = 0; i < competitions.size(); i++) {
			String result = parseEvent(i+1, competitions.get(i));
			println(result);
		}
	}

	public void processConferences() {
		if (conferences.size() == 0) {
			return;
		}
		Collections.sort(conferences, comparator);
		println("CONFERENCE:");
		for (int i = 0; i < conferences.size(); i++) {
			println(parseEvent(i+1, conferences.get(i)));
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
			builder.append(content[NAMA_LOMBA_INDEX]+"\r\n");
		}

		//tema lomba
		if (!isNullOrEmpty(content[TEMA_INDEX])) {
			builder.append(mapColumnToName.get(TEMA_INDEX));
			builder.append(": ");
			builder.append(content[TEMA_INDEX]+"\r\n");
		}

		//penyelenggara
		if (!isNullOrEmpty(content[PENYELENGGARA_INDEX])) {
			builder.append(mapColumnToName.get(PENYELENGGARA_INDEX));
			builder.append(": ");
			builder.append(content[PENYELENGGARA_INDEX]+"\r\n");
		}

		//Akhir Registrasi
		if (!isNullOrEmpty(content[AKHIR_REGIS_INDEX])) {
			builder.append(mapColumnToName.get(AKHIR_REGIS_INDEX));
			builder.append(": ");
			builder.append(formatToFullDate(content[AKHIR_REGIS_INDEX])+"\r\n");
		} else if (!isNullOrEmpty(content[DEADLINE_INDEX])) {
			builder.append(mapColumnToName.get(DEADLINE_INDEX));
			builder.append(": ");
			builder.append(formatToFullDate(content[DEADLINE_INDEX])+"\r\n");
		}

		//Tanggal Acara
		if (!isNullOrEmpty(content[TANGGAL_ACARA_INDEX])) {
			builder.append(mapColumnToName.get(TANGGAL_ACARA_INDEX));
			builder.append(": ");
			builder.append(content[TANGGAL_ACARA_INDEX]+"\r\n");
		}

		//website
		if (!isNullOrEmpty(content[WEBSITE_INDEX])) {
			builder.append(mapColumnToName.get(WEBSITE_INDEX));
			builder.append(": ");
			builder.append(content[WEBSITE_INDEX]+"\r\n");
		}

		//email
		if (!isNullOrEmpty(content[CONTACT_PERSON_INDEX])) {
			builder.append(mapColumnToName.get(CONTACT_PERSON_INDEX));
			builder.append(": ");
			builder.append(content[CONTACT_PERSON_INDEX]+"\r\n");
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

	public boolean isEventPassed(String[] obj) {
		boolean result = true;
		Date currentDate = new Date();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		if (!isNullOrEmpty(obj[AKHIR_REGIS_INDEX])) {
			try {
				Date date = df.parse(obj[AKHIR_REGIS_INDEX]);
				result = currentDate.before(date);
			} catch (ParseException e) {
				result = false;
			}
		} else if (!isNullOrEmpty(obj[DEADLINE_INDEX])) {
			try {
				Date date = df.parse(obj[DEADLINE_INDEX]);
				result = currentDate.before(date);
			} catch (ParseException e) {
				result = false;
			}
		}

		return result;
	}

	public void println(String str) {
		try {
			System.out.println(str);
			bufferedWriter.write(str+"\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			fileReader.close();
			bufferedWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}