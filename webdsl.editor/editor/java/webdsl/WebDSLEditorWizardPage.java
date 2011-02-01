package webdsl;

import java.awt.Checkbox;

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

public class WebDSLEditorWizardPage extends WizardPage {
    
    protected Text inputProjectName;
    protected String inputProjectNameValue = "";
    public String getInputProjectName() { return inputProjectNameValue; }
    protected Text inputAppName;
    protected String inputAppNameValue = "";
    public String getInputAppName() { return inputAppNameValue; }
    
    protected boolean isInputProjectNameChanged;
    protected boolean dbTypeSelected = false;
    protected boolean isMysqlDatabaseSelected;
    public boolean isMysqlSelected()  { return isMysqlDatabaseSelected; }
    public boolean isSqliteSelected() { return !isMysqlDatabaseSelected; }

    protected Label labelDBHost;
    protected Text inputDBHost;
    public String getInputDBHost() {System.out.println(inputDBHost); return inputDBHost.getText().trim(); }
    protected Label labelDBUser;
    protected Text inputDBUser;
    public String getInputDBUser() { return inputDBUser.getText().trim(); }
    protected Label labelDBPass;
    protected Text inputDBPass;
    public String getInputDBPass() { return inputDBPass.getText().trim(); }
    protected Label labelDBName;
    protected Text inputDBName;
    public String getInputDBName() { return inputDBName.getText().trim(); }
    protected Label labelDBFile;
    protected Text inputDBFile;
    public String getInputDBFile() { return inputDBFile.getText().trim(); }
    
    protected String inputDBMode;
    public String getInputDBMode() { return inputDBMode.trim(); }

    protected Text inputTomcatPath;
    public String getInputTomcatPath() { return "/opt/tomcat"; } //return inputTomcatPath.getText().trim(); }
    
    protected Text inputSmtpHost;
    public String getInputSmtpHost() { return inputSmtpHost.getText().trim(); }
    protected Text inputSmtpPort;
    public String getInputSmtpPort() { return inputSmtpPort.getText().trim(); }
    protected Text inputSmtpUser;
    public String getInputSmtpUser() { return inputSmtpUser.getText().trim(); }
    protected Text inputSmtpPass;
    public String getInputSmtpPass() { return inputSmtpPass.getText().trim(); }
    
    protected Button isRootApp;
    public boolean isRootApp() { return isRootApp.getSelection(); }
    protected Label rootAppLabel;
    
    protected boolean selectedDbMode = false;
    
    protected Group dbselectGroup;
    protected Group sqliteGroup;
    protected Group mysqlGroup;
    protected Group dbmodeGroup;
    protected Group emailGroup;
    
    protected boolean ignoreEvents;

    /**
     * Constructor for SampleNewWizardPage.
     */
    public WebDSLEditorWizardPage() {
        super("wizardPage");
        setTitle("WebDSL Project");
        setDescription("This wizard creates a new WebDSL project.");
    }
    
    public void projectAndApplicationNameEntry(Composite container){
        new Label(container, SWT.NULL).setText("&Project name:");
        inputProjectName = new Text(container, SWT.BORDER | SWT.SINGLE);
        inputProjectName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputProjectName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                inputProjectNameValue = inputProjectName.getText().trim();
                if (!ignoreEvents) {
                    distributeProjectName();
                    onChange();
                }
            }
        });
        
        inputProjectName.setFocus();
                
        new Label(container, SWT.NULL).setText("&Application name:");
        inputAppName = new Text(container, SWT.BORDER | SWT.SINGLE);
        inputAppName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputAppName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                inputAppNameValue = inputAppName.getText().trim();
                onChange();
            }
        });
    }
    
    public void addLayout(Composite container){
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        
        addLayout(container);
          
        projectAndApplicationNameEntry(container);        
        
        /*
        new Label(container, SWT.NULL).setText("&Tomcat root path (optional):");
        inputTomcatPath = new Text(container, SWT.BORDER | SWT.SINGLE);
        inputTomcatPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputTomcatPath.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (!ignoreEvents) {
                    onChange();
                }
            }
        });
        inputTomcatPath.setText("/opt/tomcat");*/
        
        
        dbselectGroup = new Group(container, SWT.NULL);
        GridData dbselectGridData = new GridData();
        dbselectGridData.horizontalAlignment = GridData.FILL;
        dbselectGridData.horizontalSpan = 2;
        dbselectGroup.setLayoutData(dbselectGridData);
        dbselectGroup.setText("Select Database (may be changed later by editing application.ini or running the 'Convert to a WebDSL project' wizard)");
        GridLayout dbselectGroupLayout = new GridLayout();
        dbselectGroup.setLayout(dbselectGroupLayout);
        dbselectGroupLayout.numColumns = 1;
        dbselectGroupLayout.verticalSpacing = 9;
        
        Button bMysql = new Button(dbselectGroup, SWT.RADIO);
        bMysql.setText("MySQL database: recommended for regular users; requires mysql installation and (empty) database created");

        Button bSqlite = new Button(dbselectGroup, SWT.RADIO);
        bSqlite.setText("Sqlite database: recommended for first-time users; limitations: no concurrent request, no File/Image support");

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
        
        (labelDBHost = new Label(mysqlGroup, SWT.NULL)).setText("&MySQL hostname:");
        inputDBHost = new Text(mysqlGroup, SWT.BORDER | SWT.SINGLE);
        inputDBHost.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputDBHost.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onChange();
            }
        });
        
        (labelDBUser = new Label(mysqlGroup, SWT.NULL)).setText("&MySQL user:");
        inputDBUser = new Text(mysqlGroup, SWT.BORDER | SWT.SINGLE);
        inputDBUser.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputDBUser.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onChange();
            }
        });
        
        (labelDBPass = new Label(mysqlGroup, SWT.NULL)).setText("&MySQL password:");
        inputDBPass = new Text(mysqlGroup, SWT.BORDER | SWT.SINGLE);
        inputDBPass.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputDBPass.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onChange();
            }
        });
        
        (labelDBName = new Label(mysqlGroup, SWT.NULL)).setText("&MySQL database name:");
        inputDBName = new Text(mysqlGroup, SWT.BORDER | SWT.SINGLE);
        inputDBName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputDBName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onChange();
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
        
        (labelDBFile = new Label(sqliteGroup, SWT.NULL)).setText("&Database file:");
        inputDBFile = new Text(sqliteGroup, SWT.BORDER | SWT.SINGLE);
        inputDBFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputDBFile.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onChange();
            }
        });
        inputDBFile.setText("temp.db");
        
        dbmodeGroup = new Group(container, SWT.NULL);
        GridData dbmodeGridData = new GridData();
        dbmodeGridData.horizontalAlignment = GridData.FILL;
        dbmodeGridData.horizontalSpan = 2;
        dbmodeGroup.setLayoutData(dbmodeGridData);
        dbmodeGroup.setText("Database mode (may be changed later by editing application.ini dbmode=create-drop/update/false or running the 'Convert to a WebDSL project' wizard)");
        GridLayout dbmodeGroupLayout = new GridLayout();
        dbmodeGroup.setLayout(dbmodeGroupLayout);
        dbmodeGroupLayout.numColumns = 1;
        dbmodeGroupLayout.verticalSpacing = 9;
        
        Button bCreateDrop1 = new Button(dbmodeGroup, SWT.RADIO);
        bCreateDrop1.setText("overwrite database when deployed: recommended for first-time users; slow, cleans database and re-creates global vars and re-runs application init blocks each time");
        bCreateDrop1.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                inputDBMode = "create-drop";
                selectedDbMode = true;
                if (!ignoreEvents) {
                    onChange();
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        Button bUpdate1 = new Button(dbmodeGroup, SWT.RADIO);
        bUpdate1.setText("update database when deployed: recommended for regular users; fast, does not clean database, updates to global vars and application init blocks require a manual database reset");
        bUpdate1.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                inputDBMode = "update";
                selectedDbMode = true;
                onChange();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        Button bFalse1 = new Button(dbmodeGroup, SWT.RADIO);
        bFalse1.setText("do not change the database when deployed: requires manually updating the database");
        bFalse1.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                inputDBMode = "false";
                selectedDbMode = true;
                onChange();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });		
        
        emailGroup = new Group(container, SWT.NULL);
        GridData emailGridData = new GridData();
        emailGridData.horizontalAlignment = GridData.FILL;
        emailGridData.horizontalSpan = 2;
        emailGroup.setLayoutData(emailGridData);
        emailGroup.setText("&Email settings (optional)");
        GridLayout emailGroupLayout = new GridLayout();
        emailGroup.setLayout(emailGroupLayout);
        emailGroupLayout.numColumns = 2;
        emailGroupLayout.verticalSpacing = 9;
        
        new Label(emailGroup, SWT.NULL).setText("SMTP Host:");
        inputSmtpHost = new Text(emailGroup, SWT.BORDER | SWT.SINGLE);
        inputSmtpHost.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputSmtpHost.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onChange();
            }
        });
        inputSmtpHost.setText("smtp.gmail.com");
        
        new Label(emailGroup, SWT.NULL).setText("SMTP Port:");
        inputSmtpPort = new Text(emailGroup, SWT.BORDER | SWT.SINGLE);
        inputSmtpPort.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputSmtpPort.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onChange();
            }
        });
        inputSmtpPort.setText("465");
        
        new Label(emailGroup, SWT.NULL).setText("SMTP User:");
        inputSmtpUser = new Text(emailGroup, SWT.BORDER | SWT.SINGLE);
        inputSmtpUser.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputSmtpUser.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onChange();
            }
        });
        
        new Label(emailGroup, SWT.NULL).setText("SMTP Password:");
        inputSmtpPass = new Text(emailGroup, SWT.BORDER | SWT.SINGLE);
        inputSmtpPass.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputSmtpPass.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onChange();
            }
        });		
        
        //database selection radio button events
        bMysql.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                pickMysql();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        bSqlite.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                pickSqlite();
            }
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
        
        isRootApp = new Button(container, SWT.CHECK);
        isRootApp.setText("&Root Application");
        rootAppLabel = new Label(container, SWT.NULL);
        rootAppLabel.setText("A root application does not include the application name in the URL.");
        
        setControl(container);
        setPageComplete(false);

    }
    
    private void pickMysql(){
        dbTypeSelected = true;
        
        sqliteGroup.setEnabled(false);
        inputDBFile.setEnabled(false);
        
        mysqlGroup.setEnabled(true);
        inputDBHost.setEnabled(true);
        inputDBName.setEnabled(true);
        inputDBPass.setEnabled(true);
        inputDBUser.setEnabled(true);
        
        mysqlGroup.setFocus();
        isMysqlDatabaseSelected = true;
        
        onChange();
    }
    private void pickSqlite(){
        dbTypeSelected = true;
        
        mysqlGroup.setEnabled(false);
        inputDBHost.setEnabled(false);
        inputDBName.setEnabled(false);
        inputDBPass.setEnabled(false);
        inputDBUser.setEnabled(false);
        
        sqliteGroup.setEnabled(true);
        inputDBFile.setEnabled(true);
        sqliteGroup.setFocus();
        isMysqlDatabaseSelected = false;
        
        onChange();
    }

    private void distributeProjectName() {
        if (!isInputProjectNameChanged || getInputAppName().length() == 0
                || getInputAppName().equals(toLanguageName(getInputProjectName()))) {
            ignoreEvents = true;
            inputAppName.setText(toLanguageName(getInputProjectName()));
            isInputProjectNameChanged = false;
            ignoreEvents = false;
        }
    }

    /**
     * Ensures that both text fields are set.
     */
    protected void onChange() {
        if (!ignoreEvents) {
            setErrorMessage(null);
            
            if (getInputProjectName().length() == 0) {
                setErrorStatus("Project name must be specified");
                return;
            }
            if (getInputAppName().length() == 0) {
                setErrorStatus("Application name must be specified");
                return;
            }	
            
            if (!isValidProjectName(getInputProjectName())) {
                setErrorStatus("Project name must be valid");
                return;
            }
            if (!toLanguageName(getInputAppName()).equalsIgnoreCase(getInputAppName())) {
                setErrorStatus("Application name must be valid");
                return;
            }
            if (!dbTypeSelected) {
                setErrorStatus("Mysql or Sqlite database must be selected");
                return;
            }
            if (!selectedDbMode) {
                setErrorStatus("Database mode must be selected");
                return;
            }
    
            if(!projectNameAvailable()){return;}
    
            if (getInputProjectName().indexOf(' ') != -1) {
                setWarningStatus("Project names with spaces may not be supported depending on your configuration");
            } else {
                setErrorStatus(null);
            }
        }
    }
    
    protected boolean projectNameAvailable() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (workspace.getRoot().getProject(getInputProjectName()).exists()) {
            setErrorStatus("A project with this name already exists");
            return false;
        }
        return true;
    }

    private static boolean isValidProjectName(String name) {
        if(!Character.isLetter(name.charAt(0))){
            return false;
        }
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

    protected void setErrorStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    protected void setWarningStatus(String message) {
        if (getErrorMessage() == null)
            setErrorMessage(message);
    }
    
}