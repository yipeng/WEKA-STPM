/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.gui.stpm.clean;

import java.io.IOException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import weka.gui.stpm.Config;
import weka.gui.stpm.GPSPoint;
import weka.gui.stpm.Trajectory;
import weka.gui.stpm.TrajectoryFrame;

/**
 *
 * @author hercules
 */
public class TrajectoryClean {

    private static FilterClean filterClean = FilterClean.getInstance();

    public static boolean isPrintSpeedToFileXls() {
        return false;
    }
    private Connection conn;
    /**User configurations*/
    private Config config = new Config();
    
    private String esquema;

    public static FilterClean getFilterClean() {
        return filterClean;
    }
    private TrajectoryFiltersCleanFrame trajectoryFiltersCleanFrame;

    public TrajectoryClean(Connection conn, List<String> listTabela) {
        this.conn = conn;
        this.filterClean.setListNomeTabelas(listTabela);
        this.trajectoryFiltersCleanFrame = new TrajectoryFiltersCleanFrame(this);
    }
    public TrajectoryClean(Connection conn) {
        this.conn = conn;
        this.trajectoryFiltersCleanFrame = new TrajectoryFiltersCleanFrame(this);
    }

    public String getEsquema() {
        if(this.esquema ==null){
            return "";
        }
        return esquema;
    }

    public void setEsquema(String esquema) {
        this.esquema = esquema;
    }

//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String args[]) {
//        ArrayList<String> lista = new ArrayList<String>();
//        lista.add("Hercules");
//        new TrajectoryClean(lista);
//    }

    public void filtrarTrajetoria() {
        
        createCleanTrajCreate();

        //for each of the trajectory table selected...
        for (String nomeTabela : this.filterClean.getListNomeTabelas()) {
//            config.table = nomeTabela;
            java.util.Date tempo, fim, ini = new java.util.Date();
            try {
//                List<TrajetoryCleanDto> listTraj = pegarDadosTrajetoria(nomeTabela);
//                String ids = retirarDados(listTraj);
                String[] nmAtts = criarNovaTabelaTrajetoria(nomeTabela);
//                List<Trajectory> listTraj = pegarDadosTrajetoria2(nomeTabela);
                getTrajectoriesWithSpeeds(nomeTabela, this.config, null, nmAtts[0], nmAtts[1]);
                

                fim = new java.util.Date();
                tempo = new java.util.Date(fim.getTime() - ini.getTime());
                inserirCleanTrajCreate(TrajectoryClean.getNomeTabNova(nomeTabela), tempo, nomeTabela);
                System.out.println("Processing time: " + tempo.getTime() + " ms");
                JOptionPane.showMessageDialog(this.trajectoryFiltersCleanFrame, "Operation finished succesfully.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this.trajectoryFiltersCleanFrame, "Error during operation");
                System.out.println("Error: \n" + e.getMessage());
            } 

        }//endof foreach
        Runtime.getRuntime().gc();


    }

    private void createCleanTrajCreate() {
//        Statement sn;
        try {
            //getting trajectory 
//            sn = this.conn.createStatement();
//            System.out.println("select relname from pg_class where relname='cleanTrajCreate'");
//            ResultSet rsn = sn.executeQuery("select relname from pg_class where relname='cleanTrajCreate'");
            ResultSet rsn = executeQuery("select relname from pg_class where lower(relname)='cleantrajcreate'");
            //se não tem a tabela, entao cria
            if (!rsn.next()) {
                StringBuilder b = new StringBuilder();
                b.append(" CREATE TABLE cleanTrajCreate ");
                b.append(" ( ");
                b.append("   id serial NOT NULL, ");
                b.append("   nomeTrajetoria character varying, ");
                b.append("   oidTrajetoria oid, ");
                b.append("   tempoLimpeza integer, ");
                b.append("   paramVeloci double precision, ");
                b.append("   paramTempo double precision, ");
                b.append("   paramQtePontos integer, ");
                b.append("   paramPorcent double precision ");
                b.append(" )  ");
//                System.out.println(b.toString());
//                sn.execute(b.toString());
                execute(b.toString());
                System.out.println("Creating tables createCleanTrajCreate...");
            }

            try {
                rsn.close();
            } catch (SQLException ex) {
                Logger.getLogger(TrajectoryClean.class.getName()).log(Level.SEVERE, null, ex);
            }
//            try {
//                sn.close();
//            } catch (SQLException ex) {
//                Logger.getLogger(TrajectoryClean.class.getName()).log(Level.SEVERE, null, ex);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
        }


    }

    private List<TrajetoryCleanDto> pegarDadosTrajetoria(String nomeTabela) {
        List<TrajetoryCleanDto> lista = new ArrayList<TrajetoryCleanDto>();
//        Statement sn;
        try {
            //getting trajectory
//            sn = this.conn.createStatement();
//            deletarSequenceLinha(nomeTabela);
//            criarSequenceLinha(nomeTabela);
            StringBuilder b = new StringBuilder();
            b.append(" CREATE TABLE temporaryTrajClean as select gid, time, tid, the_geom from ").append(nomeTabela).append(" order by tid, time, gid; ");
            b.append(" alter table temporaryTrajClean add linha serial;");
            execute(b.toString());

            b = new StringBuilder();
            b.append(" select gid, tid, tempo, tempo2, (distancia/tempo) as velocidade from ( ");
            b.append(" select t.linha, t.gid, t.tid,  ");
            b.append(" (	st_distance(t0.the_geom,t1.the_geom) + ");
            b.append(" 	st_distance(t1.the_geom,t2.the_geom) + ");
            b.append(" 	st_distance(t2.the_geom,t3.the_geom) + ");
            b.append(" 	st_distance(t3.the_geom,t4.the_geom) ");
            b.append(" )as distancia, ");
            b.append("  ");
            b.append(" ( ");
            b.append(" 	extract(EPOCH from (to_timestamp(t0.time, 'YYYY-MM-DD HH24:MI:SS')-to_timestamp(t1.time, 'YYYY-MM-DD HH24:MI:SS'))) + ");
            b.append(" 	extract(EPOCH from (to_timestamp(t1.time, 'YYYY-MM-DD HH24:MI:SS')-to_timestamp(t2.time, 'YYYY-MM-DD HH24:MI:SS'))) + ");
            b.append(" 	extract(EPOCH from (to_timestamp(t2.time, 'YYYY-MM-DD HH24:MI:SS')-to_timestamp(t3.time, 'YYYY-MM-DD HH24:MI:SS'))) + ");
            b.append(" 	extract(EPOCH from (to_timestamp(t3.time, 'YYYY-MM-DD HH24:MI:SS')-to_timestamp(t4.time, 'YYYY-MM-DD HH24:MI:SS'))) ");
            b.append(" )as tempo, ");
            b.append("  ");
            b.append(" extract(EPOCH from (to_timestamp(t2.time, 'YYYY-MM-DD HH24:MI:SS')-to_timestamp(t3.time, 'YYYY-MM-DD HH24:MI:SS'))) as tempo2 ");
            b.append(" from temporaryTrajClean t  ");
            b.append(" left join temporaryTrajClean t0 on t.linha=t0.linha-2 and t.tid=t0.tid ");
            b.append(" left join temporaryTrajClean t1 on t.linha=t1.linha-1 and t.tid=t1.tid ");
            b.append(" left join temporaryTrajClean t2 on t.linha=t2.linha   and t.tid=t2.tid ");
            b.append(" left join temporaryTrajClean t3 on t.linha=t3.linha+1 and t.tid=t3.tid ");
            b.append(" left join temporaryTrajClean t4 on t.linha=t4.linha+2 and t.tid=t4.tid ");
            b.append(" order by linha ");
            b.append(" )as foo; ");


//            System.out.println(b.toString());
//            ResultSet rsn = sn.executeQuery(b.toString());
            ResultSet rsn = executeQuery(b.toString(), "Erro na consulta da velocidade da trajetoria.");

            while (rsn.next()) {
                TrajetoryCleanDto dto = new TrajetoryCleanDto();
                dto.setGid(rsn.getLong("gid"));
                dto.setTid(rsn.getInt("tid"));
                dto.setTempo2(rsn.getDouble("tempo2"));
                dto.setTempo(rsn.getDouble("tempo"));
                dto.setVelocidade(rsn.getDouble("velocidade"));
                lista.add(dto);
            }

            b=new StringBuilder();
            b.append(" DROP TABLE temporaryTrajClean ");
            execute(b.toString());
//            deletarSequenceLinha(nomeTabela);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
    public void getTrajectoriesWithSpeeds(String tableTraj, Config config, Integer table_srid,String nmAttSelect,String nmAttGroup) throws SQLException, IOException {

        Statement s = config.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        // selects the trajectory-tid to be processed
        String sql = "SELECT "+config.tid+" as tid, count(*) FROM "+tableTraj+" GROUP BY "+config.tid+" ORDER BY tid DESC";
        ResultSet rs = s.executeQuery(sql);

    	//for each trajectory...
        while (rs.next()) {
            Trajectory trajectory = null;
            if(table_srid != null){
                trajectory = new Trajectory(table_srid);
            }else{
                trajectory = new Trajectory();
            }
            trajectory.tid = rs.getInt("tid");

            //select the points of the trajectory in sequential time
            Statement s1 = config.conn.createStatement();
            String sql2 = "SELECT "+config.time+" as time,the_geom,gid FROM "+tableTraj+" WHERE "+config.tid+"="+trajectory.tid+" ORDER BY "+config.time;
            //System.out.println(sql2);

            ResultSet rs2 = s1.executeQuery(sql2);
            //for each of these points of the trajectory...
            int timeIndex = 0;
            while (rs2.next()) {
                Timestamp t = rs2.getTimestamp("time");
                org.postgis.PGgeometry geom = (org.postgis.PGgeometry) rs2.getObject("the_geom");
                org.postgis.Point p = (org.postgis.Point) geom.getGeometry();
                //get the time, the_geom and tid columns to fill the Vector
                GPSPoint gps = new GPSPoint(trajectory.tid,t,p,timeIndex);
                gps.gid = rs2.getInt("gid");
                trajectory.points.addElement(gps);
                timeIndex++;

            }
            System.out.println("calculando velocidade da trajetoria "+trajectory.tid);
            //calculates the speed of each point, and then runs the method
//            if (TrajectoryClean.getFilterClean().getQtdePontos() != null) {
//                if (trajectory.points.size()>5){
//                    trajectory.calculatePointsSpeed();
//                }
//            }
            retirarDados3(trajectory, tableTraj, nmAttSelect, nmAttGroup  );
            System.out.println("add trajetoria " + trajectory.tid);
            System.gc();
        }
        return ;
    }
    private List<Trajectory> pegarDadosTrajetoria2(String nomeTabela) {
        List<Trajectory> lista = new ArrayList<Trajectory>();
        try {
            return TrajectoryFrame.getTrajectoriesWithSpeeds(nomeTabela, this.config, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
    private void retirarDados3(Trajectory traj, String nomeTabela, String nmAttSelect, String nmAttGroup) {
        StringBuilder ids = new StringBuilder("   ");

            Vector<GPSPoint> pontos = traj.points;

            if (TrajectoryClean.getFilterClean().getPorcentagem() != null) {
                Collections.shuffle(pontos);
                int probFim = (int)(pontos.size() * TrajectoryClean.getFilterClean().getPorcentagem() / 100);
                List<GPSPoint> listTraj0 = pontos.subList(0, probFim);
                for (GPSPoint pt : listTraj0) {
                    ids.append(pt.gid).append(", ");
                }

            } else if (TrajectoryClean.getFilterClean().getQtdePontos() != null) {
                if (TrajectoryClean.getFilterClean().getVelocidadeMaxima() != null) {
                    if (traj.points.size()>5){
                        traj.calculatePointsSpeed();
                    }
                }
                //retorna Math.round(Math.random()*5) --- de 0 a 5, incluindo o 5;
                int i = (int) Math.round(Math.random() * 5);
                while (i < traj.points.size()) {
                    GPSPoint pt = traj.points.get(i);
                    i += TrajectoryClean.getFilterClean().getQtdePontos() == null ? 1 : (this.getFilterClean().getQtdePontos()+1);
                    if (TrajectoryClean.getFilterClean().getVelocidadeMaxima() != null) {
                        if (pt.speed >= TrajectoryClean.getFilterClean().getVelocidadeMaxima()) {
                            continue;
                        }
                    }
                    if (TrajectoryClean.getFilterClean().getTempoMaximo() != null) {
    //                    if (pt.time.after(this.getFilterClean().getTempoMaximo().longValue().60000)) {
    //                        continue;
    //                    }
                    }
                    ids.append(pt.gid).append(", ");
                }
            }
            if(ids.toString().trim().length()>0){
                inserirNovaTabelaTrajetoria(nomeTabela, ids.toString().trim().substring(0, ids.toString().trim().length() - 1), nmAttSelect, nmAttGroup);
                System.out.println("Inseriu estes ids = "+ids.toString());
            }
            ids = new StringBuilder("   ");
            System.gc();
    }
    private String retirarDados2(List<Trajectory> listTraj, String nomeTabela) {
        String[] nmAtts = criarNovaTabelaTrajetoria(nomeTabela);
        StringBuilder ids = new StringBuilder("   ");

        for (Trajectory dto : listTraj) {
            Vector<GPSPoint> pontos = dto.points;

            if (TrajectoryClean.getFilterClean().getPorcentagem() != null) {
                Collections.shuffle(pontos);
                int probFim = (int)(pontos.size() * TrajectoryClean.getFilterClean().getPorcentagem() / 100);
                List<GPSPoint> listTraj0 = pontos.subList(0, probFim);
                for (GPSPoint pt : listTraj0) {
                    ids.append(pt.gid).append(", ");
                }

            } else if (this.getFilterClean().getQtdePontos() != null) {
                this.getFilterClean().getQtdePontos();

                //retorna Math.round(Math.random()*5) --- de 0 a 5, incluindo o 5;
                int i = (int) Math.round(Math.random() * 5);
                while (i < dto.points.size()) {
                    GPSPoint pt = dto.points.get(i);
                    i += this.getFilterClean().getQtdePontos() == null ? 1 : (this.getFilterClean().getQtdePontos()+1);
                    if (this.getFilterClean().getVelocidadeMaxima() != null) {
                        if (pt.speed >= this.getFilterClean().getVelocidadeMaxima()) {
                            continue;
                        }
                    }
                    if (this.getFilterClean().getTempoMaximo() != null) {
    //                    if (pt.time.after(this.getFilterClean().getTempoMaximo().longValue().60000)) {
    //                        continue;
    //                    }
                    }
                    ids.append(pt.gid).append(", ");
                }
            }
            inserirNovaTabelaTrajetoria(nomeTabela, ids.toString().trim().substring(0, ids.toString().trim().length() - 1), nmAtts[0], nmAtts[1]);
            System.out.println("Inseriu estes ids = "+ids.toString());
            ids = new StringBuilder("   ");
            System.gc();
        }
        return ids.toString().substring(0, ids.length() - 3);
    }
    private String retirarDados(List<TrajetoryCleanDto> listTraj) {

        StringBuilder b = new StringBuilder("   ");

        if (TrajectoryClean.getFilterClean().getPorcentagem() != null) {
//            Set<Integer> setIdLista = new HashSet<Integer>();
//            int count = 0;
//            System.out.println("Filtrando a trajetoria pela porcentagem.\n tam lista = "+listTraj.size());
//            while (setIdLista.size() <= (listTraj.size() * this.getFilterClean().getPorcentagem() / 100)) {
//                count++;
//                int ran   = (int) Math.round(Math.random() * (listTraj.size()-1));
//                System.out.println("num random gerado = "+ ran+" count = "+count);
//                setIdLista.add(ran);
//            }
//            for (Integer id : setIdLista) {
//                b.append(listTraj.get(id).getGid()).append(", ");
//            }
            Collections.shuffle(listTraj);
            int probFim = (int)(listTraj.size() * TrajectoryClean.getFilterClean().getPorcentagem() / 100);
            List<TrajetoryCleanDto> listTraj0 = listTraj.subList(0, probFim);
            for (TrajetoryCleanDto dto : listTraj0) {
                b.append(dto.getGid()).append(", ");
            }

        } else if (this.getFilterClean().getQtdePontos() != null) {
            this.getFilterClean().getQtdePontos();

            //retorna Math.round(Math.random()*5) --- de 0 a 5, incluindo o 5;
            int i = (int) Math.round(Math.random() * 5);
            while (i < listTraj.size()) {
                TrajetoryCleanDto traj = listTraj.get(i);
                i += this.getFilterClean().getQtdePontos() == null ? 1 : (this.getFilterClean().getQtdePontos()+1);
                if (this.getFilterClean().getVelocidadeMaxima() != null) {
                    if (traj.getVelocidade() >= this.getFilterClean().getVelocidadeMaxima()) {
                        continue;
                    }
                }
                if (this.getFilterClean().getTempoMaximo() != null) {
                    if (traj.getTempo() >= this.getFilterClean().getTempoMaximo()) {
                        continue;
                    }
                }
                b.append(traj.getGid()).append(", ");
            }
        }
        return b.toString().substring(0, b.length() - 3);
    }

    public static String getNomeTabNova(String nomeTabela) {
        if(getFilterClean().toString()!=null && !getFilterClean().toString().equals("")){
            return nomeTabela.concat("_").concat(getFilterClean().toString());
        }
        return nomeTabela;
    }

     private String[] criarNovaTabelaTrajetoria(String nomeTabela) {
        StringBuilder nmAttrsFrom    = new StringBuilder();
        StringBuilder nmAttrGroup = new StringBuilder(" tid, time ");

         //pegar os atributos da tabela attual
        try {

            StringBuilder strSelect = new StringBuilder();
            StringBuilder strInsere = new StringBuilder();
            strSelect.append(" select Att.attname,Type.typname, Att.attnotnull from pg_attribute Att ");
            strSelect.append(" JOIN pg_class Tab ON (Att.attrelid = Tab.oid)  ");
            strSelect.append(" JOIN pg_type Type ON (Att.atttypid = Type.oid)  ");
            strSelect.append(" WHERE Att.attnum > 0  ");
            strSelect.append(" and Type.typname <> 'geometry' AND Tab.relname = '").append(nomeTabela).append("' ");
            strSelect.append(" order by Att.attnum ");
            //b.append(" --and array[Att.attnum] <@ ( select conkey from pg_constraint cons where cons.conrelid=Tab.oid and cons.contype='p') ");
//            ResultSet rsAttr = stateSelect.executeQuery(strSelect.toString());
            ResultSet rsAttr = executeQuery(strSelect.toString());

            strInsere.append(" DROP TABLE ").append(this.getNomeTabNova(nomeTabela)).append("; ");
//            System.out.println( strInsere.toString());
//            stateInsert.execute(strInsere.toString());
            execute(strInsere.toString());
            strInsere = new StringBuilder();
            strInsere.append(" CREATE TABLE ").append(getNomeTabNova(nomeTabela)).append(" (  gid serial NOT NULL ");

            while (rsAttr.next()) {
                strInsere.append(", ");
                String nmAtt = rsAttr.getString("attname");
                nmAttrsFrom.append(nmAtt);
                if("gid".equalsIgnoreCase(nmAtt)){
                    nmAttrsFrom.append(" as gidOld ");
                    nmAtt="gidOld";
//                    if (!rsAttr.isLast()) {
//                       nmAttrGroup.append(", ");
//                    }
                    nmAttrGroup.append(", ");
                    nmAttrGroup.append(nmAtt);
                    
                }else if(!"tid".equalsIgnoreCase(nmAtt) && !"time".equalsIgnoreCase(nmAtt)){
                    nmAttrGroup.append(", ");
                    nmAttrGroup.append(nmAtt);
//                    if (!rsAttr.isLast()) {
//                       nmAttrGroup.append(", ");
//                    }
                }
                strInsere.append(nmAtt).append(" ");
                strInsere.append(rsAttr.getString("typname")).append(" ");
                if (rsAttr.getBoolean("attnotnull")) {
                    strInsere.append(" NOT NULL ");
                }
                if (!rsAttr.isLast()) {
                       nmAttrsFrom.append(", ");
                }
            }
            try {
                rsAttr.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            strInsere.append(" ) ");
//            System.out.println( strInsere.toString());
//            stateInsert.execute(strInsere.toString());
            execute(strInsere.toString());

            //retorna todos os nome dos atributos das chaves primarias.
            strSelect = new StringBuilder();
            strSelect.append(" select Att.attname from pg_attribute Att ");
            strSelect.append(" JOIN pg_class Tab ON (Att.attrelid = Tab.oid)  ");
            strSelect.append(" WHERE Att.attnum > 0  ");
            strSelect.append(" and array[Att.attnum] <@ ( select conkey from pg_constraint cons where cons.conrelid=Tab.oid and cons.contype='p') ");
            strSelect.append(" AND Tab.relname = '").append(nomeTabela).append("' ");
//            System.out.println(strSelect.toString());
//            rsAttr = stateSelect.executeQuery(strSelect.toString());
            rsAttr = executeQuery(strSelect.toString());

            strInsere = new StringBuilder();
            strInsere.append(" ALTER TABLE ").append(this.getNomeTabNova(nomeTabela));
            strInsere.append(" ADD CONSTRAINT ").append(this.getNomeTabNova(nomeTabela) + "_pk");
            strInsere.append(" PRIMARY KEY ( ");
            while (rsAttr.next()) {
                strInsere.append(rsAttr.getString("attname"));
                if (!rsAttr.isLast()) {
                    strInsere.append(", ");
                }
            }
            try {
                rsAttr.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            strInsere.append(" ); COMMIT; ");
            //insere chave primaria

//            System.out.println(strInsere);
//            stateInsert.execute(strInsere.toString());
            execute(strInsere.toString());


            //pega todos os nome das coluna do tipo GEOMETRY
            strSelect = new StringBuilder();
            strSelect.append(" select Att.attname from pg_attribute Att ");
            strSelect.append(" JOIN pg_class Tab ON (Att.attrelid = Tab.oid)  ");
            strSelect.append(" JOIN pg_type Type ON (Att.atttypid = Type.oid)  ");
            strSelect.append(" WHERE Att.attnum > 0 and Type.typname = 'geometry' AND Tab.relname  = '").append(nomeTabela).append("' ");
//            System.out.println(strSelect.toString());
//            rsAttr = stateSelect.executeQuery(strSelect.toString());
            rsAttr = executeQuery(strSelect.toString());

            // para cada coluna do tipo GEOMETRY na tab cria na nova
            while (rsAttr.next()) {
                String nmAttr = rsAttr.getString("attname");
                nmAttrsFrom.append(", ").append(nmAttr);
                nmAttrGroup.append(", ").append(nmAttr);
                String str = "select distinct st_srid(" +nmAttr
                        + ") as srid, st_ndims(" +nmAttr
                        + ") as ndims, geometrytype(" +nmAttr
                        + ") as geometrytype from " + nomeTabela;
//                System.out.println(str);
//                ResultSet rsAttrGeo = stateSelect2.executeQuery(str);
                ResultSet rsAttrGeo = executeQuery(str);
                strInsere = new StringBuilder();
                strInsere.append("SELECT AddGeometryColumn('"
                        + this.getNomeTabNova(nomeTabela) + "','"
                        +nmAttr + "','");

                if(rsAttrGeo.next()){
                    strInsere.append(rsAttrGeo.getInt("srid") + "','"
                        + rsAttrGeo.getString("geometrytype") + "',"
                        + rsAttrGeo.getInt("ndims"));
                }else{
                     strInsere.append(" '-1', 'POINT', 2 ");
                }
                strInsere.append(" ) ");
//                System.out.println(strInsere.toString());
//                stateInsert.execute(strInsere.toString());
                execute(strInsere.toString());
                try {
                    rsAttrGeo.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            try {
                rsAttr.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }



        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new String[]{nmAttrsFrom.toString(), nmAttrGroup.toString()};
    }



    private void inserirNovaTabelaTrajetoria(String nomeTabela, String ids, String nmAttrsSelect, String nmAttrsGroup) {
        //pegar os atributos da tabela attual
            StringBuilder strInsere = new StringBuilder();
            String nmTabNova = TrajectoryClean.getNomeTabNova(nomeTabela);
            strInsere.append("INSERT INTO ").append(nmTabNova);
            strInsere.append(" SELECT nextval('").append(nmTabNova).append("_gid_seq'), ").append(nmAttrsSelect).append(" from ").append(nomeTabela);
            strInsere.append(" where gid in ( ").append(ids).append(" ) ");
            strInsere.append(" group by ").append(nmAttrsGroup);
            strInsere.append(" order by tid, time ");
            execute(strInsere.toString(), "Erro ao inserir dados na nova trajetória.");


    }

    private void inserirCleanTrajCreate(String nomeTabelaNova, Date tempoCriacaoNova, String nomeTabelaVelha) {
        //pegar os atributos da tabela attual
//        try {
//            Statement stateInsert = this.conn.createStatement();

            StringBuilder strInsere = new StringBuilder();
            strInsere.append("INSERT INTO cleantrajcreate ");
            strInsere.append(" ( nomeTrajetoria, oidTrajetoria, tempoLimpeza, pontosAntes, pontosDepois , paramVeloci, paramTempo, paramQtePontos, paramPorcent )");
            strInsere.append(" VALUES ( '").append(nomeTabelaNova).append("', ");
            strInsere.append(" null ").append(", '");
            strInsere.append(tempoCriacaoNova.getTime()).append("', ");

            strInsere.append(" (select count(*) from ").append(nomeTabelaNova ).append(" ) , ");
            strInsere.append(" (select count(*) from ").append(nomeTabelaVelha).append(" ) , ");
            if(this.getFilterClean().getVelocidadeMaxima()==null){
                strInsere.append(" null ");
            }else{
                strInsere.append(this.getFilterClean().getVelocidadeMaxima());
            }
            strInsere.append(", ");
            if(this.getFilterClean().getTempoMaximo()==null){
                strInsere.append(" null ");
            }else{
                strInsere.append(this.getFilterClean().getTempoMaximo());
            }
            strInsere.append(", ");
            
            if(this.getFilterClean().getQtdePontos()==null){
                strInsere.append(" null ");
            }else{
                strInsere.append(this.getFilterClean().getQtdePontos());
            }
            strInsere.append(", ");

            if(this.getFilterClean().getPorcentagem()==null){
                strInsere.append(" null ");
            }else{
                strInsere.append(this.getFilterClean().getPorcentagem());
            }
            strInsere.append(" ) ");
//            System.out.println(strInsere.toString());
//            stateInsert.execute(strInsere.toString());
            execute(strInsere.toString());

//            try {
//                stateInsert.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }

//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
    }

    public void setVisibleFrame(boolean b) {
        this.trajectoryFiltersCleanFrame.setVisible(b);
    }

    private void execute(String sql, String msg) {
       Statement state = null;
        try {
            state = this.conn.createStatement();
            System.out.println(sql);
            state.execute(sql);
            state.close();
        } catch (SQLException ex) {
            Logger.getLogger(TrajectoryClean.class.getName()).log(Level.SEVERE, null, ex);
            if(msg != null){
                msg = "".equals(msg)?"Erro na consulta do banco de dados":msg;
                JOptionPane.showMessageDialog(this.trajectoryFiltersCleanFrame, msg);
            }
        }

    }
    private void execute(String sql) {
       String msg = null;
       this.execute(sql, msg);
    }



    private ResultSet executeQuery(String sql, String msg) {
       Statement state = null;
       ResultSet rsn = null;
        try {
            state = this.conn.createStatement();
            System.out.println(sql);
            rsn = state.executeQuery(sql);
//            state.close();
        } catch (SQLException ex) {
            Logger.getLogger(TrajectoryClean.class.getName()).log(Level.SEVERE, null, ex);
            if(msg != null){
                msg = "".equals(msg)?"Erro na consulta do banco de dados":msg;
                JOptionPane.showMessageDialog(this.trajectoryFiltersCleanFrame, msg);
            }
        }
        return rsn;
    }
    private ResultSet executeQuery(String sql) {
        String msg = null;
        return executeQuery(sql, msg);
    }

    private void deletarSequenceLinha(String nomeTabela) {
        
    }

    private void criarSequenceLinha(String nomeTabela) {

    }

    /**
     * @return the config
     */
    public Config getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(Config config) {
        this.config = config;
    }
}
