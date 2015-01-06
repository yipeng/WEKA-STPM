/*
 * TrajectoryConfig.java
 *
 * Created on 8 de Agosto de 2007, 16:21
 */

package weka.gui.stpm;

import java.sql.*;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author  Administrador
 */
public class TrajectoryConfig extends JDialog {
    private Config config;
    private String[] genTid,genTime;
    
    /** Creates new form TrajectoryConfig */
    public TrajectoryConfig() {
        initComponents();
    }
    
    private void scriptsGenTid() {
        jTextArea.setText(
        "BEGIN;\n"+
        "CREATE SEQUENCE serial;\n" +
        "CREATE TABLE traj_temp (gid  integer,nome character varying) WITHOUT OIDS;\n" +
        "INSERT INTO traj_temp(gid,nome) SELECT DISTINCT ON (<label>) nextval('serial'), <label> FROM <trajectories>;\n" + 
        "ALTER TABLE <trajectories> ADD COLUMN \"tid\" integer;\n" + 
        "UPDATE <trajectories> A SET tid = (SELECT t.gid FROM traj_temp T WHERE T.nome = A.<label> LIMIT 1);\n" +
        "DROP TABLE traj_temp;\n" +
        "DROP SEQUENCE serial;\n" +
        "COMMIT;");
        jTextArea.setWrapStyleWord(false);
    }
    
    private void scriptsGenTime() {
/*Script that works in postgres 8.2
        jTextArea.setText(
            "BEGIN;\n" +
            "ALTER TABLE <trajectories> ADD COLUMN \"time\" timestamp without time zone;\n" +
            "UPDATE <trajectories> SET time = CAST(('2006-06-'|| (1 + (Cast(<time_a> as int)/86400)) ||' '|| ((Cast(<time_a> as int)%86400)/3600)||':'|| Cast(<time_a> as int)%86400%3600 / 60 || ':' "+
            "|| Cast(<time_a> as int)%86400%3600 % 60) as timestamp);\n" +
            "COMMIT;");                
 */
//Script that works in postgres 7.x        
        jTextArea.setText(
            "BEGIN;\n" +
            "ALTER TABLE <trajectories> ADD COLUMN \"time\" timestamp without time zone;\n" +
            "UPDATE <trajectories> SET time = CAST(('2006-06-'|| (1 + (substr(<time_a>,1,6)::int)/86400) ||' '|| (substr(<time_a>,1,6)::int)%86400/3600||':'|| "+
            "(substr(<time_a>,1,6)::int)%86400%3600 / 60 || ':' || (substr(<time_a>,1,6)::int)%86400%3600 % 60) as timestamp);\n" +
            "COMMIT;");                        
        jTextArea.setWrapStyleWord(false);
    }
    
    public void setConfig(Config conf) {
        config = conf;
        jLabelTrajectoryTable.setText(config.table);
        jComboBoxTid.removeAllItems();
        jComboBoxTime.removeAllItems();
        try {
            Statement s = config.conn.createStatement();
            String sql = "SELECT Att.attname,Type.typname FROM pg_attribute Att JOIN pg_class Tab ON (Att.attrelid = Tab.oid) " +
                         " JOIN pg_type Type ON (Att.atttypid = Type.oid) WHERE Att.attnum > 0 AND Tab.relname = '"+config.table+"'";
            //System.out.println(sql);
            
            ResultSet rs = s.executeQuery(sql);
            
            while (rs.next()) {
                Attribute att = new Attribute(rs.getString("attname"),rs.getString("typname"));
                jComboBoxTid.addItem(att);
                jComboBoxTime.addItem(att);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        if (config.tid != null) {
            for (int i=0;i<jComboBoxTid.getItemCount();i++) {
                Attribute a = (Attribute) jComboBoxTid.getItemAt(i);
                if (a.name.compareTo(config.tid) ==0 ) {
                    jComboBoxTid.setSelectedIndex(i);
                    break;
                }
            }
        }
        if (config.time != null) {
            for (int i=0;i<jComboBoxTime.getItemCount();i++) {
                Attribute a = (Attribute) jComboBoxTime.getItemAt(i);
                if (a.name.compareTo(config.time) ==0 ) {
                    jComboBoxTime.setSelectedIndex(i);
                    break;
                }
            }            
        }
    }
    
    private void checkTid() {
        for (int i=0;i<jComboBoxTid.getItemCount();i++) {
            Attribute a = (Attribute) jComboBoxTid.getItemAt(i);
            if (a.name.compareTo("tid") == 0 ) {
                try {
                    Statement s = config.conn.createStatement();
                    s.execute("ALTER TABLE "+config.table+" RENAME tid TO tidOld");
                }catch (SQLException ex) {
                    ex.printStackTrace();
                }                
                break;
            }
        }
    }
    
    private void checkTime() {
        for (int i=0;i<jComboBoxTime.getItemCount();i++) {
            Attribute a = (Attribute) jComboBoxTime.getItemAt(i);
            if (a.name.compareTo("time") ==0 ) {
                try {
                    Statement s = config.conn.createStatement();
                    s.execute("ALTER TABLE "+config.table+" RENAME time TO timeOld");
                }catch (SQLException ex) {
                    ex.printStackTrace();
                }                                
                break;
            }
        }                    
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Código Gerado ">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JButton jButtonCancel;
        javax.swing.JButton jButtonExecuteScript;
        javax.swing.JButton jButtonOk;
        javax.swing.JLabel jLabel1;
        javax.swing.JLabel jLabel2;
        javax.swing.JLabel jLabel3;
        javax.swing.JPanel jPanel1;
        javax.swing.JPanel jPanel2;
        javax.swing.JScrollPane jScrollPane1;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabelTrajectoryTable = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboBoxTid = new javax.swing.JComboBox();
        jComboBoxTime = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jRadioButtonGenTid = new javax.swing.JRadioButton();
        jRadioButtonTime = new javax.swing.JRadioButton();
        jButtonExecuteScript = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea = new javax.swing.JTextArea();
        jButtonCancel = new javax.swing.JButton();
        jButtonOk = new javax.swing.JButton();

        setTitle("Trajectory Table Config");
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel1.setText("Trajectory Table:");

        jLabelTrajectoryTable.setFont(new java.awt.Font("Tahoma", 2, 14));
        jLabelTrajectoryTable.setText("jLabel2");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Table Attributes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 13), new java.awt.Color(0, 0, 0)));
        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14));
        jLabel2.setText("Trajectory Id:");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14));
        jLabel3.setText("Trajectory Time:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(17, 17, 17)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBoxTime, 0, 250, Short.MAX_VALUE)
                    .addComponent(jComboBoxTid, 0, 250, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBoxTid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBoxTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Scripts", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 13), new java.awt.Color(0, 0, 0)));
        buttonGroup1.add(jRadioButtonGenTid);
        jRadioButtonGenTid.setText("Generate Numeric Tid");
        jRadioButtonGenTid.setToolTipText("Creates the column tid by numerating the distinct names of the trajectory id choosed above");
        jRadioButtonGenTid.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonGenTid.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButtonGenTid.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonGenTidItemStateChanged(evt);
            }
        });

        buttonGroup1.add(jRadioButtonTime);
        jRadioButtonTime.setText("Generate Timestamp From Int");
        jRadioButtonTime.setToolTipText("Creates the column time, which type is timestamp, from a numeric time choosed above");
        jRadioButtonTime.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonTime.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButtonTime.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonTimeItemStateChanged(evt);
            }
        });

        jButtonExecuteScript.setText("Execute Script");
        jButtonExecuteScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExecuteScriptActionPerformed(evt);
            }
        });

        jTextArea.setColumns(20);
        jTextArea.setRows(5);
        jScrollPane1.setViewportView(jTextArea);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jRadioButtonGenTid)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButtonTime))
                    .addComponent(jButtonExecuteScript, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonGenTid)
                    .addComponent(jRadioButtonTime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonExecuteScript))
        );

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jButtonOk.setText("Ok");
        jButtonOk.setPreferredSize(new java.awt.Dimension(71, 25));
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTrajectoryTable, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(272, Short.MAX_VALUE)
                .addComponent(jButtonOk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCancel)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabelTrajectoryTable))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonOk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExecuteScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExecuteScriptActionPerformed
        if (jRadioButtonGenTid.isSelected()) {
            checkTid();
            config.tid = "tid";
        }else if (jRadioButtonTime.isSelected()) {
            checkTime();
            config.time = "time";
        }

        Statement s = null;
        try {
            s = config.conn.createStatement();
            s.execute(jTextArea.getText());
            JOptionPane.showMessageDialog(this,"Script performed sucessfully");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,ex.toString());
            try {
                s.execute("ROLLBACK;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        setConfig(config);
    }//GEN-LAST:event_jButtonExecuteScriptActionPerformed

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        config.tid = ((Attribute) jComboBoxTid.getSelectedItem()).name;
        config.time = ((Attribute) jComboBoxTime.getSelectedItem()).name;
        this.dispose();
    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jRadioButtonTimeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonTimeItemStateChanged
        if (jRadioButtonTime.isSelected()) {
            scriptsGenTime();
            String sql = jTextArea.getText();
            sql = sql.replace("<trajectories>",config.table);
            Attribute a = (Attribute) jComboBoxTime.getSelectedItem();
            sql = sql.replace("<time_a>",a.name);
            jTextArea.setText(sql);
        }
    }//GEN-LAST:event_jRadioButtonTimeItemStateChanged

    private void jRadioButtonGenTidItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonGenTidItemStateChanged
        if (jRadioButtonGenTid.isSelected()) {
            scriptsGenTid();
            String sql = jTextArea.getText();
            sql = sql.replace("<trajectories>",config.table);
            Attribute a = (Attribute) jComboBoxTid.getSelectedItem();
            sql = sql.replace("<label>",a.name);
            jTextArea.setText(sql);
        }
    }//GEN-LAST:event_jRadioButtonGenTidItemStateChanged
    
    
    // Declaração de variáveis - não modifique//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox jComboBoxTid;
    private javax.swing.JComboBox jComboBoxTime;
    private javax.swing.JLabel jLabelTrajectoryTable;
    private javax.swing.JRadioButton jRadioButtonGenTid;
    private javax.swing.JRadioButton jRadioButtonTime;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextArea jTextArea;
    // Fim da declaração de variáveis//GEN-END:variables
    
    class Attribute {
        String name;
        String type;
        
        Attribute(String n,String t) {
            name = n;
            type = t;
        }
        
        public String toString() {
            return name + " ("+type+")";
        }
    }
}
