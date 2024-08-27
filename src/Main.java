import java.io.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.math.BigInteger;

import static java.math.RoundingMode.HALF_UP;

public class Main {

    enum Type {
        INTEGER,
        DOUBLE,
        STRING,
        ERROR
    }

    static class Stat {
        int intNoteNumber = 0, doubleNoteNumber = 0, strNoteNumber = 0;
        BigInteger intMin = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger intMax = BigInteger.valueOf(Long.MIN_VALUE);
        BigDecimal intAverage = BigDecimal.ZERO;

        BigDecimal doubleMin = BigDecimal.valueOf(Double.MAX_VALUE);
        BigDecimal doubleMax = BigDecimal.valueOf(Double.MIN_VALUE);
        BigDecimal doubleAverage = BigDecimal.ZERO;

        long strMaxLen = Long.MIN_VALUE, strMinLen = Long.MAX_VALUE;
        double strAverageLen = 0;
    }
    public static void main(String[] args)  {
        if(checkArgs(args)) {
            boolean addInFileMode=findIndexInArray(args,"-a")!=-1;//добавлять в файл результат(1) или перезаписать его(0)
            boolean shortStatistics=findIndexInArray(args,"-s")!=-1;//надо ли собирать краткую статистику
            boolean fullStatistics=findIndexInArray(args,"-f")!=-1;//надо ли собирать полную статистику
            boolean specialPath=findIndexInArray(args,"-o")!=-1;//надо ли результат в полный путь записывать
            boolean prefixNeeded=findIndexInArray(args,"-p")!=-1;//Нужен ли префикс перед файлами
            String path,prefix;

            if(specialPath){
                String userHome = System.getProperty("user.home");
                File directory = new File(userHome, args[findIndexInArray(args,"-o")+1]);
                path=directory.toString();
            }
            else path= System.getProperty("user.dir");
            System.out.println(path);
            path+='\\'; //получили путь
            if(prefixNeeded) prefix=args[findIndexInArray(args,"-p")+1];
            else prefix="";

            deleteExistedFiles(path,prefix,addInFileMode);

            int numberOfFiles=0;
            for (String arg : args) if (arg.indexOf(".txt") > 0) numberOfFiles++;
            String[] names=new String[numberOfFiles];
            int count=0;
            for (String arg : args)
                if (arg.indexOf(".txt") > 0) {
                    names[count] = arg;
                    //System.out.println(names[count]);
                    count++;
                }

            BufferedReader[] reader = new BufferedReader[numberOfFiles];
            Stat stat=new Stat();
                try {
                    for(int i=0;i<numberOfFiles;i++) {
                        reader[i] = new BufferedReader(new FileReader(names[i]));
                    }
                    boolean flag=true;
                    while (flag) {
                        int numberOfNulls=0;
                        for (int i = 0; i < numberOfFiles; i++) {
                            String line = reader[i].readLine();
                            if(line==null) numberOfNulls++;
                            else {
                                filterLine(path,line,prefix,stat,fullStatistics,shortStatistics);
                            }
                        }
                        if(numberOfNulls==numberOfFiles) flag=false;
                    }
                    for(int i=0;i<numberOfFiles;i++) reader[i].close();

                    printStat(stat,fullStatistics,shortStatistics);
                }
                catch (IOException e) {
                    System.out.println("ERROR: COULDN'T FIND INPUT FILE");
                }

        }



            //filterFiles(path,"test.txt","int.txt","dbl.txt","str.txt");

    }

   static void printStat(Stat stat, boolean fullStat, boolean shortStat) {
        if(shortStat) {
            System.out.println("Sorted integers - "+stat.intNoteNumber);
            System.out.println("Sorted doubles - "+stat.doubleNoteNumber);
            System.out.println("Sorted strings - "+stat.strNoteNumber);
        }
        if(fullStat) {
            if(stat.intNoteNumber==0) System.out.print("Integer:\nnumber of notes - 0 \tmax note - n/a\t min note - n/a\t average - n/a\n");
            else {
                BigDecimal noteNumberOfInt = new BigDecimal(stat.intNoteNumber),average=new BigDecimal(stat.intAverage.toString());
                average=average.setScale(6,HALF_UP);
                average=average.divide(noteNumberOfInt,HALF_UP);
                System.out.printf("Integer:\nnumber of notes - %d\tmax note - %s\t min note - %s\t average - %s\n", stat.intNoteNumber, stat.intMax.toString(), stat.intMin.toString(),average );
            }
            if(stat.doubleNoteNumber==0) System.out.print("Double:\nnumber of notes - 0 \tmax note - n/a\t min note - n/a\t average - n/a\n");
            else{
                BigDecimal noteNumberOfDouble = new BigDecimal(stat.doubleNoteNumber);
                stat.doubleMax=stat.doubleMax.setScale(6,HALF_UP);
                stat.doubleMin=stat.doubleMin.setScale(6,HALF_UP);
                stat.doubleAverage=stat.doubleAverage.setScale(6,HALF_UP);
                System.out.printf("Double:\nnumber of notes - %d\tmax note - %s\t min note - %s\t average - %s\n",stat.doubleNoteNumber,stat.doubleMax,stat.doubleMin,stat.doubleAverage.divide(noteNumberOfDouble,HALF_UP));

            }
            if(stat.strNoteNumber==0) System.out.print("String:\nnumber of notes - 0 \tmax note - n/a\t min note - n/a\t average - n/a\n");
            else System.out.printf("String:\nnumber of notes - %d\tmax note - %d\t min note - %d\t average - %.2f\n",stat.strNoteNumber,stat.strMaxLen,stat.strMinLen,stat.strAverageLen/stat.strNoteNumber);
        }

    }



   static void deleteExistedFiles(String path,String prefix, boolean addInFileMode) {
        if(!addInFileMode) {
            File integer = new File(path+prefix+"integers.txt");
            File doubles = new File(path+prefix+"doubles.txt");
            File strs = new File(path+prefix+"strings.txt");
            integer.delete();
            doubles.delete();
            strs.delete();
        }//удаление ненужных файлов, если перезапись!
    }
    static void filterLine(String path, String str, String prefix, Stat stat, boolean fullStatistics, boolean shortStatistics) {
        //System.out.println(str);
            str=str.trim();
            if(!str.isEmpty()) {
                Type stringType = checkType(str);
                if (stringType == Type.INTEGER) {
                    writeToFile(path + prefix + "integers.txt", str);
                    changeStat(str,stat,fullStatistics,shortStatistics,Type.INTEGER);
                } else if (stringType == Type.DOUBLE) {
                    writeToFile(path + prefix + "doubles.txt", str);
                    changeStat(str,stat,fullStatistics,shortStatistics,Type.DOUBLE);
                }
                else if (stringType == Type.STRING) {
                    writeToFile(path + prefix + "strings.txt", str);
                    changeStat(str,stat,fullStatistics,shortStatistics,Type.STRING);
                }
            }
    }

static void changeStat(String str, Stat stat,boolean fullStatistics, boolean shortStatistics, Type type) {
    if(type==Type.INTEGER) {
        if (shortStatistics) stat.intNoteNumber++;
        else if (fullStatistics) {
            stat.intNoteNumber++;
            BigInteger num=new BigInteger(str);

            stat.intMax= stat.intMax.compareTo(num)>0?stat.intMax:num;
            stat.intMin= stat.intMin.compareTo(num)<0?stat.intMin:num;
            BigDecimal add=new BigDecimal(str);
            stat.intAverage=stat.intAverage.add(add);
        }
    }
    else if(type==Type.DOUBLE) {
        if (shortStatistics) stat.doubleNoteNumber++;
        else if (fullStatistics) {
            BigDecimal num=new BigDecimal(str);
            stat.doubleNoteNumber++;
            stat.doubleMax=stat.doubleMax.compareTo(num)>0? stat.doubleMax:num;
            stat.doubleMin=stat.doubleMin.compareTo(num)<0? stat.doubleMin:num;
            stat.doubleAverage=stat.doubleAverage.add(num);
        }
    }
    else if(type==Type.STRING) {
        if (shortStatistics) stat.strNoteNumber++;
        else if (fullStatistics) {
            int len=str.length();
            stat.strNoteNumber++;
            stat.strMaxLen = Math.max(stat.strMaxLen, len);
            stat.strMinLen = Math.min(stat.strMinLen, len);
            stat.strAverageLen+=len;
        }

    }
}

   static boolean checkArgs(String [] args) {
        if(args.length==0) {
            System.out.println("ERROR: ADD FILES TO FILTER");
            return false;
        }
        if(containsDuplicate(args)){
            System.out.println("ERROR: DUPLICATED KEYS(-o -a -p etc.) OR CHOSE -f AND -s IN ONE TIME");
            return false;//обработать повторения ключей
        }
        for(int i=0;i<args.length;i++) {
            if(args[i].matches("-\\w")&& !(args[i].equals("-o")||args[i].equals("-p")||args[i].equals("-a")||args[i].equals("-s")||args[i].equals("-f"))) {
                System.out.println("ERROR: PREFIX DOESN'T EXIST");
                return false; //если есть неправильные префиксы
            }

            else if(args[i].equals("-o")&&i<args.length-1&&!isDirectory(args[i+1])) {
                if(i==args.length-1) {
                    System.out.println("ERROR : -o IN THE END");
                }
                String userHome = System.getProperty("user.home");
                File directory = new File(userHome, args[i+1]);
                try{directory.mkdirs();

                }
                catch(SecurityException e) {
                    System.out.println("ERROR: CAN'T CREATE DIRECTORY");
                }
            }

            else if(args[i].equals("-p")&&i<args.length-1&&!isPrefix(args[i+1])) { //если некорректный префикс
                System.out.println("ERROR: CAN'T USE GIVEN PREFIX");
                return false;
            }
        }
return true;
    }
    static int findIndexInArray(String [] array, String find) {
        for(int i=0;i<array.length;i++) if(find.equals(array[i])) return i;
        return -1;
    }
    static boolean containsDuplicate(String[] args) {//works
        Set <String> set=new HashSet<>();
        for(String str:args) {
            if(set.contains(str)||(str.equals("-s")&&set.contains("-f"))||(str.equals("-f")&&set.contains("-s"))) return true;
            else set.add(str);
        }

        return false;
    }
static boolean isDirectory(String str) {
        File file=new File(str);
        return file.isDirectory();
}


    static boolean isPrefix(String str) { //works
        return str.matches("[a-zA-Z0-9_\\-]+");

    }

static void writeToFile(String path,String message) {
    try {
        FileWriter writer = new FileWriter(path, true);
        BufferedWriter bufferWriter = new BufferedWriter(writer);
        bufferWriter.write(message+"\n");
        bufferWriter.close();
    } catch (IOException e) {
        System.out.println("ERROR: COULDN'T WRITE TO FILE: " + e.getMessage());
    }
}

    static  boolean isDouble(String in) {
        return (in.matches("-?\\d+\\.?\\d+") || in.matches("-?\\d+\\.?\\d+[eE]-?\\d+")) && !isInteger(in);
    }

    static boolean isInteger(String in) {

        return in.matches("-?\\d+");
    }
static boolean isString(String in) {
        return !in.isEmpty();
    }
    static Type checkType(String str) {
        if(isDouble(str)){
           // System.out.println("It is double");
            return Type.DOUBLE;
        }
        else if(isInteger(str)) {
           // System.out.println("it is integer");
            return Type.INTEGER;

        }
        else if(isString(str)) {
           // System.out.println("It is string");
            return Type.STRING;
        }
        else {
           // System.out.println("ERROR: NOT INTEGER, DOUBLE OR STRING");
            return Type.ERROR;
        }
    }
}
