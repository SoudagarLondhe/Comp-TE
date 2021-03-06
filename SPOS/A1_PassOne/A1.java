
package sposl;
import java.io.*;
import java.util.*;

class Data
{
    String class_name;
    String code;
    int length;
    public Data(String class_name,String code,int length)
    {
        this.class_name=class_name;
        this.code=code;
        this.length=length;
    }
}
class Literaldata
{
    int index;
    String name;
    int address;
    public Literaldata(int index,String name,int address)
    {
        this.index=index;
        this.name=name;
        this.address=address;
    }
}
class Symboldata
{
    int index;
    int address;
    public Symboldata(int index,int address)
    {
        this.index=index;
        this.address=address;
    }
}
class Pass1
{
    HashMap<String,Data> mot; 
    HashMap<String,Data> pot;
    HashMap<String,Data> dls;
    HashMap<String,String> bcn;
    HashMap<String,String> reg;
    
    LinkedHashMap<String,Symboldata> Symtable;  
    LinkedHashMap<Integer,Literaldata> Littable; 
    ArrayList<String> LitKeys;
    
    HashMap<Integer,Integer> PoolTable;
    
    public Pass1()
    {
        mot=new HashMap<String,Data>();
        pot=new HashMap<String,Data>();
        dls=new HashMap<String,Data>();
        bcn=new HashMap<String,String>();
        reg=new HashMap<String,String>();
         
       Symtable=new LinkedHashMap<String,Symboldata>();
       Littable=new LinkedHashMap<Integer,Literaldata>();
       LitKeys = new ArrayList<String>();
       PoolTable=new LinkedHashMap<Integer,Integer>();
      
       initial();
    }
    public void initial()
    {
        mot.put("STOP",new Data("IS","00",1));    
        mot.put("ADD",new Data("IS","01",1));     
        mot.put("SUB",new Data("IS","02",1));     
        mot.put("MULT",new Data("IS","03",1));     
        mot.put("MOVER",new Data("IS","04",1));     
        mot.put("MOVEM",new Data("IS","05",1));     
        mot.put("COMP",new Data("IS","06",1));     
        mot.put("BC",new Data("IS","07",1));     
        mot.put("DIV",new Data("IS","08",1));     
        mot.put("READ",new Data("IS","09",1));     
        mot.put("PRINT",new Data("IS","10",1)); 
        
        pot.put("START",new Data("AD","01",00));
        pot.put("END",new Data("AD","02",00));
        pot.put("ORIGIN",new Data("AD","03",00));
        pot.put("EQU",new Data("AD","04",00));
        pot.put("LTORG",new Data("AD","05",00));
        
        dls.put("DC",new Data("DL","01",01));
        dls.put("DS",new Data("DL","02",01));
        
        bcn.put("LT","1");
        bcn.put("LE","2");
        bcn.put("EQ","3");
        bcn.put("GT","4");
        bcn.put("GE","5");
        bcn.put("ANY","6");
        
        reg.put("AREG","1");
        reg.put("BREG","2");
        reg.put("CREG","3");
        reg.put("DREG","4");
      
    }
    int check(String s)
    {
        if(mot.containsKey(s) || pot.containsKey(s))
        {
            return 0;
        }
       return 1;
    }
    public void readfile() throws Exception
    {
       File f=new File("Input.asm");
       Scanner input=new Scanner(f);
 
       Symboldata sd;
       FileWriter fw = new FileWriter("IC.txt");
      
       int val;
       int LC=0,Stp=0,Ltp=0,Ptp=1;
       Data d1;
       String str1,str2;
       PoolTable.put(0,0);
        String[] arr;
       while(input.hasNext())
       {
           String str=input.nextLine();
           str=str.trim();
           arr=str.split("\\s+");
           int j=0;
           int x=check(arr[0]);
           if(x==1)
           {
              if(Symtable.containsKey(arr[j]))
              {
                  Symboldata sd1=Symtable.get(arr[j]);
                  sd1.address=LC;
              }
              else
              {
                  Symtable.put(arr[j],new Symboldata(Stp,LC));
                  Stp++;
              }
              j++;
           }
           //for next or first
           if(arr[j].equals("START"))
           {
               Data d=pot.get("START");
               str1="-    ( "+d.class_name+" , "+d.code+" ) - ";
               val=Integer.parseInt(arr[j+1]);
               str2="( C , "+String.valueOf(val)+" )";
               fw.write(str1+str2+"\n");
               LC=val;
           }
           else if(arr[j].equals("ORIGIN"))
           {
               String[] arr1=arr[j+1].split("\\+");              
               str1="-    ( "+pot.get("ORIGIN").class_name+" , "+pot.get("ORIGIN").code+" ) - ";
                if(Symtable.containsKey(arr1[0]))
                {
                    str2="( S , "+Symtable.get(arr1[0]).index+" )+"+arr1[1];           
                    int adr=Symtable.get(arr1[0]).address;
                    LC=Integer.parseInt(arr1[1])+adr;
                }
                else
                {
                    str2="( C , "+arr1[0]+" )";
                    LC=Integer.parseInt(arr1[0]);
                }
                fw.write(str1+str2+"\n");
           }
           else if(arr[j].equals("LTORG") || arr[j].equals("END"))
           {
               d1=pot.get(arr[j]);
               String s1="-    ( "+d1.class_name+" , "+d1.code+" ) - -";
                fw.write(s1+"\n");
               int start=PoolTable.get(Ptp-1);
               for(int k=start;k < Ltp;k++)
               {
                   Literaldata ld=Littable.get(k);
                   ld.address=LC;
                   String s2=ld.name;
                   s2=s2.replace("=","").replace("'","");
                   String s3=LC+"  ( DL , "+dls.get("DC").code+" ) - ( C , "+s2+" )";
                   fw.write(s3+"\n");
                   LC++;
               }
               if(arr[j].equals("LTORG"))
               {
                   PoolTable.put(Ptp,Ltp);
                   Ptp++;
               }
           }
           else if(arr[j].equals("BC"))
           {
               str1=LC+"  ( "+mot.get("BC").class_name+" , "+mot.get("BC").code+" ) ";
               str1=str1+"("+bcn.get(arr[j+1])+")";
               if(Symtable.containsKey(arr[j+3]))
               {
                   //if Symbol table contains
                   sd=Symtable.get(arr[j+3]);                   
                   str2=" ( S , "+sd.index+" )";
               }
               else
               {
                   //if not contain in Symt
                   Symtable.put(arr[j+3],new Symboldata(Stp,00));
                   str2=" ( S , "+Stp+" )";
                   Stp++;
               }
               fw.write(str1+str2+"\n");
              // LC++;
               LC=LC+mot.get("BC").length;
           }
           else if(arr[j].equals("STOP"))
           {
               str1=LC+"  ( "+mot.get("STOP").class_name+" , "+mot.get("STOP").code+" ) - -";
               fw.write(str1+"\n");
               //LC++;
               LC=LC+mot.get("STOP").length;
           }
           else if(arr[j].equals("EQU"))
           {
               str1="-    ( "+pot.get("EQU").class_name+" , "+pot.get("EQU").code+" ) - ";
               int ta;
               if(arr[j+1].contains("+"))
               {
                   String[] arr1=arr[j+1].split("\\+");
                   str2="( S , "+Symtable.get(arr1[0]).index+" )+"+Integer.parseInt(arr1[1]);
                   ta=Symtable.get(arr1[0]).address+Integer.parseInt(arr1[1]);
               }
               else if(arr[j+1].contains("-"))
               {
                   String[] arr1=arr[j+1].split("\\-");
                   str2="( S , "+Symtable.get(arr1[0]).index+" )-"+Integer.parseInt(arr1[1]);
                   ta=Symtable.get(arr1[0]).address-Integer.parseInt(arr1[1]);
               }
               else
               {
                   str2="( S , "+Symtable.get(arr[j+1]).index+" )";
                   ta=Symtable.get(arr[j+1]).address;
               }
                Symtable.get(arr[0]).address=ta;
                fw.write(str1+str2+"\n");
           }
           else if(arr[j].equals("DS"))
           {
               str1=LC+"  ( "+dls.get("DS").class_name+" , "+dls.get("DS").code+" ) - ";
               str2="( C , "+arr[j+1]+" )";
               fw.write(str1+str2+"\n");
               LC=LC+Integer.parseInt(arr[j+1]);
           }
           else if(arr[j].equals("DC"))
           {
               String str3=arr[j+1].replace("'","");
               str1=LC+"  ( "+dls.get("DC").class_name+" , "+dls.get("DC").code+" ) - ";
               str2="( C , "+str3+" )";
               fw.write(str1+str2+"\n");
               LC=LC+dls.get("DC").length;
           }
           else
            {           
              d1=mot.get(arr[j]);
              String temp;
               str1=LC+"  ( "+d1.class_name+" , "+d1.code+" ) ";
               if(arr[j].equals("PRINT"))
               {
                  str1=str1+"-";
                  temp=arr[j+1];
               }
               else
               {
                   String regi="("+reg.get(arr[j+1])+")";
                   str1=str1+regi;
                   temp=arr[j+3];
               }
               if(temp.charAt(0)=='=')
               {      
                   int index=presentinpool(PoolTable.get(Ptp-1),temp);
                   if(index==-1)
                   {
                       Littable.put(Ltp,new Literaldata(Ltp,temp,00));
                       LitKeys.add(temp);
                       str2=" ( L , "+Ltp+" )";
                       Ltp++;
                   }
                   else
                   {
                       str2=" ( L , "+index+" )";
                   }
               }
               else if(Symtable.containsKey(temp))
               {
                   //if Symbol table contains
                   sd=Symtable.get(temp);                   
                   str2=" ( S , "+sd.index+" )";
               }
               else
               {
                   //if not contain in Symt
                   Symtable.put(temp,new Symboldata(Stp,00));
                   str2=" ( S , "+Stp+" )";
                   Stp++;
               }
                fw.write(str1+str2+"\n");
                LC=LC+mot.get(arr[j]).length;
           }
        }
        fw.close();
    }
    int presentinpool(int j,String str)
    {  
        for(int i=j;i<LitKeys.size();i++)
        {
            if(LitKeys.get(i).equals(str))
            {
                return i;
            }
        }
        return -1;
    }
}
public class A1
{
    public static void main(String[] args) throws Exception
    {
         Pass1 p=new Pass1();
         p.readfile();
          FileWriter lt = new FileWriter("Literal_table.txt");
          FileWriter st = new FileWriter("Symbol_table.txt");
          FileWriter pt = new FileWriter("Pool_table.txt");
          
         System.out.println("\nLiteral Table=>");
         System.out.println("LNo.\tLiteral\tAddress");
        for( Map.Entry<Integer, Literaldata> entry : p.Littable.entrySet() )
        {
            System.out.println(entry.getValue().index+"\t"+entry.getValue().name+"\t"+entry.getValue().address);
            lt.write(entry.getValue().index+"\t"+entry.getValue().name+"\t"+entry.getValue().address+"\n");
        }
        
        System.out.println("\nSymbol Table=>");
        System.out.println("SNo.\tSymbol\tAddress");
        for( Map.Entry<String, Symboldata> entry : p.Symtable.entrySet() )
        {
            System.out.println(entry.getValue().index+"\t"+entry.getKey()+"\t"+entry.getValue().address);
            st.write(entry.getValue().index+"\t"+entry.getKey()+"\t"+entry.getValue().address+"\n");
        }
        
        System.out.println("\nPool Table=>");
        for( Map.Entry<Integer,Integer> entry : p.PoolTable.entrySet() )
        {
            System.out.println("#"+entry.getValue());
            pt.write("#"+entry.getValue()+"\n");
        }
        lt.close();
        st.close();
        pt.close();
    }   
}
