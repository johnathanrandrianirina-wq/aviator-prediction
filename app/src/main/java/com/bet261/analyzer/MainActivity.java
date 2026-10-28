package com.bet261.analyzer;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import org.json.*;
import java.util.*;

public class MainActivity extends Activity {
  ArrayList<Double> values = new ArrayList<>();
  LinearLayout root, history;
  TextView stats;
  Graph graph;
  EditText entry;
  TextView simulated;
  Random random = new Random();
  int navy = Color.rgb(12,19,37), panel=Color.rgb(26,37,59), white=Color.WHITE, teal=Color.rgb(55,220,190);
  @Override public void onCreate(Bundle b){super.onCreate(b); load(); render();}
  TextView text(String s,int size,int color){ TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(8,10,8,10);return t; }
  void render(){
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(22,28,22,24);root.setBackgroundColor(navy);scroll.addView(root);setContentView(scroll);
    root.addView(text("✈  AVIATOR • SIMULATEUR",22,teal));
    root.addView(text("BET261 • Démonstration indépendante • Hors ligne",13,Color.LTGRAY));
    LinearLayout inputRow=new LinearLayout(this);inputRow.setOrientation(0);
    entry=new EditText(this);entry.setSingleLine(true);entry.setHint("Ex. 2.45");entry.setHintTextColor(Color.GRAY);entry.setTextColor(white);entry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);inputRow.addView(entry,new LinearLayout.LayoutParams(0,-2,1));
    Button add=new Button(this);add.setText("AJOUTER");inputRow.addView(add);root.addView(inputRow);
    add.setOnClickListener(v->{try{double n=Double.parseDouble(entry.getText().toString().trim().replace(',','.'));if(!Double.isFinite(n)||n<1||n>1000000)throw new Exception();values.add(n);save();entry.setText("");refresh();}catch(Exception e){Toast.makeText(this,"Saisir un multiplicateur valide (≥ 1.00)",Toast.LENGTH_LONG).show();}});
    LinearLayout simPanel=new LinearLayout(this);simPanel.setOrientation(1);simPanel.setPadding(20,18,20,20);simPanel.setBackgroundColor(panel);
    simPanel.addView(text("SIMULATION ALÉATOIRE — PAS UNE PRÉDICTION",14,teal));
    simulated=text("— x",36,white);simPanel.addView(simulated);
    simPanel.addView(text("Valeur fictive générée localement. Elle ne correspond pas au prochain tour de BET261.",12,Color.LTGRAY));
    Button simulate=new Button(this);simulate.setText("GÉNÉRER UNE SIMULATION");simPanel.addView(simulate);
    simulate.setOnClickListener(v->{double r=random.nextDouble();double sample=1.0+Math.min(99.0,-Math.log(Math.max(0.000001,1-r))*2.0);simulated.setText(String.format(Locale.US,"%.2fx",sample));});
    root.addView(simPanel);
    root.addView(text("STATISTIQUES DE VOS RÉSULTATS",16,teal));
    stats=text("",16,white);stats.setBackgroundColor(panel);root.addView(stats);
    root.addView(text("ÉVOLUTION DES MULTIPLICATEURS",16,teal));graph=new Graph(this);root.addView(graph,new LinearLayout.LayoutParams(-1,260));
    LinearLayout actions=new LinearLayout(this);Button undo=new Button(this);undo.setText("ANNULER DERNIER");actions.addView(undo,new LinearLayout.LayoutParams(0,-2,1));undo.setOnClickListener(v->{if(!values.isEmpty()){values.remove(values.size()-1);save();refresh();}});
    Button clear=new Button(this);clear.setText("EFFACER TOUT");actions.addView(clear,new LinearLayout.LayoutParams(0,-2,1));clear.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Effacer l'historique ?").setMessage("Cette action est irréversible.").setNegativeButton("Annuler",null).setPositiveButton("Effacer",(d,w)->{values.clear();save();refresh();}).show());root.addView(actions);
    root.addView(text("HISTORIQUE (du plus récent au plus ancien)",16,teal));history=new LinearLayout(this);history.setOrientation(1);root.addView(history);
    root.addView(text("Aucun accès au jeu, aucun signal en temps réel. Les simulations sont indépendantes des résultats réels. Ne misez pas en vous fiant à ces chiffres.",12,Color.LTGRAY));refresh();
  }
  void refresh(){int n=values.size();double sum=0,min=Double.MAX_VALUE,max=0;int low=0,mid=0,high=0;for(double v:values){sum+=v;min=Math.min(min,v);max=Math.max(max,v);if(v<2)low++;else if(v<10)mid++;else high++;}
    if(n==0)stats.setText("Aucun résultat enregistré. Ajoutez un multiplicateur.");else stats.setText(String.format(Locale.US,"Tours : %d    Moyenne : %.2fx\nMinimum : %.2fx    Maximum : %.2fx\n< 2x : %.1f%%   |   2x à < 10x : %.1f%%\n≥ 10x : %.1f%%",n,sum/n,min,max,100.0*low/n,100.0*mid/n,100.0*high/n));
    history.removeAllViews();for(int i=n-1;i>=Math.max(0,n-60);i--){double v=values.get(i);TextView t=text(String.format(Locale.US,"#%d     %.2fx",i+1,v),15,v>=10?teal:(v>=2?white:Color.rgb(255,143,150)));history.addView(t);}graph.invalidate();}
  void load(){try{JSONArray a=new JSONArray(getPreferences(0).getString("history","[]"));for(int i=0;i<a.length();i++){double v=a.getDouble(i);if(Double.isFinite(v)&&v>=1)values.add(v);}}catch(Exception ignored){}}
  void save(){JSONArray a=new JSONArray();for(double v:values)a.put(v);getPreferences(0).edit().putString("history",a.toString()).apply();}
  class Graph extends View{Paint p=new Paint(3);Graph(Context c){super(c);setBackgroundColor(panel);}void line(Canvas c,float x,float y,float x2,float y2,int color,float width){p.setColor(color);p.setStrokeWidth(width);c.drawLine(x,y,x2,y2,p);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);int n=Math.min(values.size(),40);float w=getWidth(),h=getHeight(),left=36,right=w-15,top=22,bottom=h-30;line(c,left,bottom,right,bottom,Color.GRAY,2);if(n==0)return;double max=2;for(int i=values.size()-n;i<values.size();i++)max=Math.max(max,values.get(i));p.setTextSize(25);p.setColor(Color.LTGRAY);c.drawText(String.format(Locale.US,"max %.2fx",max),left,25,p);float lastX=0,lastY=0;for(int j=0;j<n;j++){double v=values.get(values.size()-n+j);float x=left+(right-left)*(n==1?0.5f:j/(float)(n-1));float y=bottom-(float)(v/max)*(bottom-top);if(j>0)line(c,lastX,lastY,x,y,teal,4);p.setColor(teal);c.drawCircle(x,y,5,p);lastX=x;lastY=y;}}
  }
}
