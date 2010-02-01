package webdsl;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.preferences.fields.RadioGroupFieldEditor;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (esv).
 */

public class WebDSLEditorWizardPage extends WizardPage {
	
	private Text inputProjectName;
	public String getInputProjectName() { return inputProjectName.getText().trim(); }
	private Text inputLanguageName;
	public String getInputLanguageName() { return inputLanguageName.getText().trim(); }
	
	private boolean isInputProjectNameChanged;
	private boolean isMysqlDatabaseSelected;
	public boolean isMysqlSelected()  { return isMysqlDatabaseSelected; }
	public boolean isSqliteSelected() { return !isMysqlDatabaseSelected; }

	private Text inputDBHost;
	public String getInputDBHost() { return inputDBHost.getText().trim(); }
	private Text inputDBUser;
	public String getInputDBUser() { return inputDBUser.getText().trim(); }
	private Text inputDBPass;
	public String getInputDBPass() { return inputDBPass.getText().trim(); }
	private Text inputDBName;
	public String getInputDBName() { return inputDBName.getText().trim(); }
	private String inputDBMode;
	public String getInputDBMode() { return inputDBMode.trim(); }
	private Text inputDBFile;
	public String getInputDBFile() { return inputDBFile.getText().trim(); }
	
	private Group sqliteGroup;
	private Group mysqlGroup;
	private Group dbmodeGroup;
	
	private boolean ignoreEvents;

	/**
	 * Constructor for SampleNewWizardPage.
	 */
	public WebDSLEditorWizardPage() {
		super("wizardPage");
		setTitle("WebDSL Project");
		setDescription("This wizard creates a WebDSL project.");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
				
		new Label(container, SWT.NULL).setText("&Project name:");
		inputProjectName = new Text(container, SWT.BORDER | SWT.SINGLE);
		inputProjectName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputProjectName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					distributeProjectName();
					onChange();
				}
			}
		});
				
		new Label(container, SWT.NULL).setText("&Application name:");
		inputLanguageName = new Text(container, SWT.BORDER | SWT.SINGLE);
		inputLanguageName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputLanguageName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					onChange();
				}
			}
		});
		
		dbmodeGroup = new Group(container, SWT.NULL);
		GridData dbmodeGridData = new GridData();
		dbmodeGridData.horizontalAlignment = GridData.FILL;
		dbmodeGridData.horizontalSpan = 2;
		dbmodeGroup.setLayoutData(dbmodeGridData);
		dbmodeGroup.setText("Database mode");
		GridLayout dbmodeGroupLayout = new GridLayout();
		dbmodeGroup.setLayout(dbmodeGroupLayout);
		dbmodeGroupLayout.numColumns = 2;
		dbmodeGroupLayout.verticalSpacing = 9;
		
		Button bCreateDrop1 = new Button(dbmodeGroup, SWT.RADIO);
		bCreateDrop1.setText("overwrite database when deployed");
		bCreateDrop1.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				inputDBMode = "create-drop";
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	    Button bUpdate1 = new Button(dbmodeGroup, SWT.RADIO);
	    bUpdate1.setText("update database when deployed");
	    bUpdate1.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				inputDBMode = "update";
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    
	    Button bFalse1 = new Button(dbmodeGroup, SWT.RADIO);
	    bFalse1.setText("don't change the database");
	    bFalse1.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				inputDBMode = "false";
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	    Button bMysql = new Button(container, SWT.RADIO);
	    bMysql.setText("MySQL database");

	    Button bSqlite = new Button(container, SWT.RADIO);
	    bSqlite.setText("Sqlite database");

		mysqlGroup = new Group(container, SWT.NULL);
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		mysqlGroup.setLayoutData(gridData);

		mysqlGroup.setText("MySQL configuration");
		
		GridLayout mysqlGroupLayout = new GridLayout();
		mysqlGroup.setLayout(mysqlGroupLayout);
		mysqlGroupLayout.numColumns = 2;
		mysqlGroupLayout.verticalSpacing = 9;
		
		new Label(mysqlGroup, SWT.NULL).setText("&MySQL hostname:");
		inputDBHost = new Text(mysqlGroup, SWT.BORDER | SWT.SINGLE);
		inputDBHost.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputDBHost.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					onChange();
				}
			}
		});
		
		new Label(mysqlGroup, SWT.NULL).setText("&MySQL user:");
		inputDBUser = new Text(mysqlGroup, SWT.BORDER | SWT.SINGLE);
		inputDBUser.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputDBUser.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					onChange();
				}
			}
		});
		
		new Label(mysqlGroup, SWT.NULL).setText("&MySQL password:");
		inputDBPass = new Text(mysqlGroup, SWT.BORDER | SWT.SINGLE);
		inputDBPass.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputDBPass.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					onChange();
				}
			}
		});
		
		new Label(mysqlGroup, SWT.NULL).setText("&MySQL database name:");
		inputDBName = new Text(mysqlGroup, SWT.BORDER | SWT.SINGLE);
		inputDBName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputDBName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					onChange();
				}
			}
		});
		
		sqliteGroup = new Group(container, SWT.NULL);
		
		GridData sqliteGridData = new GridData();
		sqliteGridData.horizontalAlignment = GridData.FILL;
		sqliteGridData.horizontalSpan = 2;
		sqliteGroup.setLayoutData(sqliteGridData);

		sqliteGroup.setText("Sqlite configuration");
		
		GridLayout sqliteGroupLayout = new GridLayout();
		sqliteGroup.setLayout(sqliteGroupLayout);
		sqliteGroupLayout.numColumns = 2;
		sqliteGroupLayout.verticalSpacing = 9;
		
		new Label(sqliteGroup, SWT.NULL).setText("&Database file:");
		inputDBFile = new Text(sqliteGroup, SWT.BORDER | SWT.SINGLE);
		inputDBFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputDBFile.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					onChange();
				}
			}
		});
		inputDBFile.setText("temp.db");
	    
		//database selection radio button events
	    bMysql.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pickMysql();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    bSqlite.addSelectionListener(new SelectionListener() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		pickSqlite();
	    	}
	    	@Override
	    	public void widgetDefaultSelected(SelectionEvent e) {
	    	}
	    });
	    
	    sqliteGroup.setEnabled(false);
     	inputDBFile.setEnabled(false);
		mysqlGroup.setEnabled(false);
		inputDBHost.setEnabled(false);
		inputDBName.setEnabled(false);
		inputDBPass.setEnabled(false);
		inputDBUser.setEnabled(false);
	    
		setControl(container);
		setPageComplete(false);
		
		inputProjectName.setFocus();
	}
	
	private void pickMysql(){
		sqliteGroup.setEnabled(false);
		inputDBFile.setEnabled(false);
		
		mysqlGroup.setEnabled(true);
		inputDBHost.setEnabled(true);
		inputDBName.setEnabled(true);
		inputDBPass.setEnabled(true);
		inputDBUser.setEnabled(true);
		
		mysqlGroup.setFocus();
		isMysqlDatabaseSelected = true;
	}
	private void pickSqlite(){
		mysqlGroup.setEnabled(false);
		inputDBHost.setEnabled(false);
		inputDBName.setEnabled(false);
		inputDBPass.setEnabled(false);
		inputDBUser.setEnabled(false);
		
		sqliteGroup.setEnabled(true);
		inputDBFile.setEnabled(true);
		sqliteGroup.setFocus();
		isMysqlDatabaseSelected = false;
	}

	private void distributeProjectName() {
		if (!isInputProjectNameChanged || getInputLanguageName().length() == 0
				|| getInputLanguageName().equals(toLanguageName(getInputProjectName()))) {
			ignoreEvents = true;
			inputLanguageName.setText(toLanguageName(getInputProjectName()));
			isInputProjectNameChanged = false;
			ignoreEvents = false;
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void onChange() {
		setErrorMessage(null);
		
		if (getInputProjectName().length() == 0) {
			setErrorStatus("Project name must be specified");
			return;
		}
		if (getInputLanguageName().length() == 0) {
			setErrorStatus("Application name must be specified");
			return;
		}	
		
		if (!isValidProjectName(getInputProjectName())) {
			setErrorStatus("Project name must be valid");
			return;
		}
		if (!toLanguageName(getInputLanguageName()).equalsIgnoreCase(getInputLanguageName())) {
			setErrorStatus("Application name must be valid");
			return;
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace.getRoot().getProject(getInputProjectName()).exists()) {
			setErrorStatus("A project with this name already exists");
			return;
		}

		if (getInputProjectName().indexOf(' ') != -1) {
			setWarningStatus("Project names with spaces may not be supported depending on your configuration");
		} else {
			setErrorStatus(null);
		}
	}

	private static boolean isValidProjectName(String name) {
		for (char c : name.toCharArray()) {
			if (!(Character.isLetterOrDigit(c) || c == '_' || c == ' ' || c == '-' || c == '.'
				|| c == '(' || c == ')' || c == '#' || c == '+' || c =='[' || c == ']' || c == '@'))
				return false;
		}
		return true;
	}

	private static String toLanguageName(String name) {
		return name;
		/*
		char[] input = name.replace(' ', '-').toCharArray();
		StringBuilder output = new StringBuilder();
		int i = 0;
		while (i < input.length) {
			char c = input[i++];
			if (Character.isLetter(c) || c == '-' || c == '_') {
				output.append(c);
				break;
			}
		}
		while (i < input.length) {
			char c = input[i++];
			if (Character.isLetterOrDigit(c) || c == '-' || c == '_')
				output.append(c);
		}
		if (output.length() > 0)
			output.setCharAt(0, Character.toUpperCase(output.charAt(0))); // SDF wants a capital here
		return output.toString();*/
	}
	
	private static String toPackageName(String name) {
		char[] input = name.replace(' ', '-').toCharArray();
		StringBuilder output = new StringBuilder();
		int i = 0;
		while (i < input.length) {
			char c = input[i++];
			if (Character.isLetter(c) || c == '.' || c == '_') {
				output.append(c);
				break;
			}
		}
		while (i < input.length) {
			char c = input[i++];
			if (Character.isLetterOrDigit(c) || c == '.' || c == '_')
				output.append(c);
		}
		return output.toString();
	}
	
	private static String toExtension(String name) {
		String input = name.toLowerCase().replace("-", "").replace(".", "").replace(" ", "").replace(":", "");
		String prefix = input.substring(0, Math.min(input.length(), 3));
		if (input.length() == 0) return "";
		
		for (int i = input.length() - 1;; i--) {
			if (!Character.isDigit(input.charAt(i)) && input.charAt(i) != '.') {
				return prefix + input.substring(Math.max(prefix.length(), Math.min(input.length(), i + 1)));
			} else if (i == prefix.length()) {
				return prefix + input.substring(i);
			}
		}
	}

	private void setErrorStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	private void setWarningStatus(String message) {
		if (getErrorMessage() == null)
			setErrorMessage(message);
	}
	
}