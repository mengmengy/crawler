package org.gwu.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.gwu.dao.ParserDAO;
import org.gwu.model.Job;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

public class Parser {
	private static final int PAGES = 2;
	private static final String TITLE_XPATH = "//div[@class = 'internship-result-link-item']/h3/a";	
	private static final String NAME_XPATH = "//div[@class = 'internship-detail-header']/h1";
	private static final String EMP_XPATH = "//div[@class = 'company-name']/span";
	private static final String LOC_XPATH = "//div[@itemprop = 'jobLocation']/div";
	private static final String DEADLINE_XPATH = "//div[@class = 'section i-info']/div/span";
	//private static final String POSITN_XPATH = "////div[@class = 'section i-info']/div/span']";
	private static final String DEC_XPATH = "//div[@itemprop = 'description']";
	private static final String REQ_XPATH = "//div[@itemprop = 'skills']";
	
	private static TagNode node;
	
	public static void main(String[] args) throws IOException, XPatherException{
		ArrayList<Job> rec = new ArrayList<Job>();
		ParserDAO pd = new ParserDAO();
		ArrayList<String> kw = new ArrayList<String>();
		//Requirement 表里面的所有skill，事先写好存在数据库里面的
		kw = pd.getKeywords();
		System.out.print("Please wait ...");
		
		for(int i = 1; i <= PAGES; i++){
			String opUrl = "http://www.internships.com/search/posts?Keywords=%22computer%20science%22%2C%20programmer%2C%20%22web%20developer%22%2C%20IT%2C%20developer%2C%20technology%2C%20software&page=" + String.valueOf(i);
			
			HtmlCleaner htmlCleaner = new HtmlCleaner();
			CleanerProperties props = new CleanerProperties();
			props.setAllowHtmlInsideAttributes(true);
	        props.setAllowMultiWordAttributes(true);
	        props.setRecognizeUnicodeChars(true);
	        props.setOmitComments(true);
			
	        URL url = new URL(opUrl);
	        //URLConnection conn = url.openConnection();
	        
	        //node is xml tree
	        node = htmlCleaner.clean(url);
	        
	        //store every node of xml tree to titleNodes. (every job on the web)
	        Object[] titleNodes = node.evaluateXPath(TITLE_XPATH);
	        
			for(Object temp : titleNodes){
				TagNode tn = (TagNode) temp;
				Job r = new Job();
				//System.out.println(tn.getText());
				//System.out.println("http://www.internships.com" + tn.getAttributeByName("href"));
				String detailUrl = "http://www.internships.com" + tn.getAttributeByName("href");
				URL newUrl = new URL(detailUrl);
				//conn = newUrl.openConnection();	
				//xml tree of each job
		        node = htmlCleaner.clean(newUrl);
		        
		        //find the nodes which fulfill the path of NAME_XPATH
		        titleNodes = node.evaluateXPath(NAME_XPATH);
		        if(titleNodes.length == 1){			        
					TagNode childTn = (TagNode) titleNodes[0];
					//through the function(getText()) of htmlcleaner to get the text we want.
					String name = childTn.getText().toString().trim();
					if(name.length() > 50)
						name = name.substring(0, 50);
					r.setJobName(name);			  
		        }
		        
		        //company name
		        titleNodes = node.evaluateXPath(EMP_XPATH);
		        if(titleNodes.length == 1){
					TagNode childTn = (TagNode) titleNodes[0];
					r.setEmployer(childTn.getText().toString().trim());
		        }
		        //location
		        titleNodes = node.evaluateXPath(LOC_XPATH);
		        if(titleNodes.length == 2){
		        	String loc = "";
		        	TagNode childTn = (TagNode) titleNodes[0];
		        	loc = childTn.getText().toString().trim();
		        	childTn = (TagNode) titleNodes[1];
		        	loc = loc.concat(", " + childTn.getText().toString().trim());
		        	while(loc.length() > 50)
		        		loc = loc.substring(loc.indexOf(",")+1).trim();
		        	r.setLocation(loc);
		        }
		        //deadline
		        titleNodes = node.evaluateXPath(DEADLINE_XPATH);
		        if(titleNodes.length == 2){
		        	TagNode childTn = (TagNode) titleNodes[0];
		        	r.setDeadline(childTn.getText().toString().trim());
		        	childTn = (TagNode) titleNodes[1];
		        	String s = childTn.getText().toString().trim();
					String[] tmp = s.split(",");
					
					String ss = tmp[1].trim();
					if(ss.length() > 20)
						ss = ss.substring(0, 20);
					r.setCompensation(ss);	
					if(tmp[0].contains(" ") == false)
						r.setType(tmp[0].trim());
					else{
						String[] tmp2 = tmp[0].split(" ");
						r.setOpening(Integer.parseInt(tmp2[0].trim()));
						r.setType(tmp2[1].trim());
					}       	
		        }
		        //description
		        titleNodes = node.evaluateXPath(DEC_XPATH);
		        if(titleNodes.length == 1){
					TagNode childTn = (TagNode) titleNodes[0];
					r.setDes(childTn.getText().toString().trim());
		        }
		        //requirement
		        titleNodes = node.evaluateXPath(REQ_XPATH);
		        if(titleNodes.length == 1){
					TagNode childTn = (TagNode) titleNodes[0];
					//s就表示网页上的一整段requirement
					String s = childTn.getText().toString().trim().toLowerCase();
					ArrayList<String> reqs = new ArrayList<String>();
					//比较那一段话里面的每一词是不是一个skill，如果是就把这个skill加到reqs里面
					for(String word : kw)
					{
						if(s.contains(word.toLowerCase()))
						{
							reqs.add(word);
						}
					}
					//根据requirement给每一个job加上skill的tag
					r.setReq(reqs);
		        }
		        rec.add(r);
		        System.out.print("...");
			}			
		}
		pd.save2DB(rec);
        System.out.println("Done. " + PAGES*10 + " records are added to the DB.");
	}	
}
