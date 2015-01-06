package weka.gui.stpm;

import java.util.Comparator;
import java.util.Vector;
import java.util.Arrays;
/*
 * Conjunto de todas as intersecções entre pontos das trajetórias e Relevant Features.
 * Esses pontos são obtidos externamente com a query: 
 * 
 */

public class InterceptsG {
	
	public Vector<Interc> intercepts;	
	//public ArrayList<int> pontos;
	
	public InterceptsG(){
		this.intercepts=new Vector<Interc>();
	}
	
	public void addpt(int ponto,int gid,String rf,int val){
		Interc novo=new Interc(ponto,gid,rf,val);
		//a medida que é adicionado um novo elemento, organiza o vetor
		if(intercepts.size()>0){
			int id=0;
			//tenta achar o local-'id' onde deve ser posto o ponto novo
			while(ponto>(intercepts.get(id)).pt && id<=(intercepts.size()-1)){
				id++;
			}
			intercepts.add(id,novo);//adiciona na posição encontrada
		}
		else intercepts.add(novo); //caso raiz
		
	}
	
	public void addpt(Interc i){
		//a medida que é adicionado um novo elemento, organiza o vetor
		if(intercepts.size()>0){
			int id=0;
			//tenta achar o local-'id' onde deve ser posto o ponto novo
			while(id<=(intercepts.size()-1) && i.pt>(intercepts.get(id)).pt){
				id++;
			}
			intercepts.add(id,i);//adiciona na posição encontrada
		}
		else intercepts.add(i); //caso raiz
	}
	
	public void listar(){
		for(int i=0;i<intercepts.size();i++){
			Interc a=intercepts.get(i);
			System.out.println("\nPt= "+a.pt+"\tGid="+a.gid+"\trf="+a.rf);
		}
	}
	
	public void tamanho(){
		System.out.println("\nTamanho da estrutura: "+intercepts.size());
	}
	
	// Para retornar um rf que intercepta aquele ponto.	
	public Interc is_in(int ponto){
/*
		int pos,fim=(intercepts.size()-1),inicio=0;
		Interc key = new Interc(ponto,0,"",0);
		Interc retorno;
		pos=intercepts.size()/2;		
		retorno=null;
		//enquanto eu nao achar ninguem, e enquanto nao der colisao dos indicadores de inicio e fim com a pos
		while(retorno==null && pos!=fim && pos!=inicio){
			//achou o cara com aquela chave-'pt'
			if((intercepts.get(pos)).compareTo(key)==0){
				retorno=intercepts.get(pos);
			}
			//key < valor[pos],tem q procurar na esquerda
			else if((intercepts.get(pos)).compareTo(key)>0){
				fim=pos;
				pos=(fim-inicio)/2;//deslocamento... 
				pos+=inicio;//...em relação ao inicio
			}
			//key > valor[pos],tem q procurar na direita
			else{
				inicio=pos;
				pos=(fim-inicio)/2;//deslocamento...
				pos+=inicio;//...em relação ao inicio
			}
		}
*/
		Interc key = new Interc(ponto,0,"",0);
		Interc retorno = null;
		int i = 0;
		while ((retorno == null) && (i <= intercepts.size()-1)) {
			if ((intercepts.get(i)).compareTo(key)==0) {
				retorno = intercepts.get(i);
			}
			i++;
		}
		return retorno;
	}
	
	public Interc getRFIntercept(int gidPonto, int gidRF) {
		Interc key = new Interc(gidPonto,gidRF,"",0);
		Interc retorno = null;
		int i = 0;
		while (retorno == null && (i <= intercepts.size()-1)) {
			if ((intercepts.get(i)).compareToByGids(key)) {
				retorno = intercepts.get(i);
			}
			i++;
		}
		return retorno;
	}
	
	// retorna o campo 'valor' do rf que intercepta aquele ponto
	//utilizado para retornar a velocidade limite de uma rua, dado um ponto nela situado
	public int value(int ponto){
		Interc a = is_in(ponto);
		if(a!=null) return a.value;
		else return 0;
	}
	
	// Para retornar uma lista de RFs que interceptam aquele ponto
		
	public Vector<Interc> is_in2(int chave){
		int pos,fim=(intercepts.size()-1),inicio=0;
		Interc key = new Interc(chave,0,"",0);
		Vector<Interc> retorno= new Vector<Interc>();
		pos=intercepts.size()/2;
		//enquanto eu nao achar ninguem, e enquanto nao der colisao dos indicadores de inicio e fim com a pos
		while(retorno.isEmpty() && pos!=fim && pos!=inicio){
			//achou o cara com aquela chave-'pt'
			if((intercepts.get(pos)).compareTo(key)==0){
				retorno.add(intercepts.get(pos));
				pos++;
				while((intercepts.get(pos)).compareTo(key)==0){
					retorno.add(intercepts.get(pos));
					pos++;
				}
			}
			//key < valor[pos],tem q procurar na esquerda
			else if((intercepts.get(pos)).compareTo(key)>0){
				fim=pos;
				pos=(fim-inicio)/2;//deslocamento... 
				pos+=inicio;//...em relação ao inicio
			}
			//key > valor[pos],tem q procurar na direita
			else{
				inicio=pos;
				pos=(fim-inicio)/2;//deslocamento...
				pos+=inicio;//...em relação ao inicio
			}
		}
		
		return retorno;
	}
}
