package com.wtgroup.sugar.str;

import cn.hutool.core.util.StrUtil;

import java.util.*;
import java.text.*;
import static java.lang.System.*;

/**
 * @deprecated 待完善
 */
public class StringAlign extends Format {
	
	private static final long serialVersion = 112432535L;
	
	/*枚举，哪种对齐方式*/
	public enum Alignment{
		/*左对齐*/
		LEFT,
		/*居中对齐*/
		CENTER,
		/*右对齐*/
		RIGHT,
	}
	
	private Alignment aligment;//当前对齐
	private int maxPages;//当前最大长度
	
	/*构造方法，用来设置字符串的居中方式以及最大长度*/
	public StringAlign(int maxPages, Alignment alignment) {
		
		switch(alignment) {
		case LEFT:
		case CENTER:
		case RIGHT:
			this.aligment = alignment;//将传过来的对齐方式赋值给全局的alignment变量
			break;
			
		default:
			throw new IllegalArgumentException("对齐参数错误！");
		}
		
		if(maxPages < 0) {//长度为负数时会抛出异常
			throw new IllegalArgumentException("页数参数错误");
		}
		
		this.maxPages = maxPages;
		
	}
 

	@Override
	public StringBuffer format(Object input, StringBuffer where, FieldPosition ignore) {
		// TODO Auto-generated method stub
		String s = input.toString();
		// 超出的, 截取
		// String wanted = s.substring(0,Math.min(len(s), maxPages));
		String wanted = sub(s, maxPages);

		//得到右侧的空格
		int lenWanted = len(wanted);
		switch(aligment) {
		case RIGHT:
			pad(where, maxPages - lenWanted);
			where.append(wanted);
			break;
		case CENTER:
			int toAdd = maxPages - lenWanted;
			int howMany = Math.toIntExact(Math.round(toAdd / 2.0));
			pad(where, howMany);
			where.append(wanted);
			pad(where, howMany);
			break;
		case LEFT:
			where.append(wanted);
			pad(where, maxPages- lenWanted);
			break;
		}
		return where;
	}
 
	private void pad(StringBuffer where, int howMany) {
		for(int i = 0; i < howMany; i++) {
			where.append(' ');//添加空格
		}
	}
	
	String format(String s) {
		return format(s, new StringBuffer(), null).toString();
	}
 
	@Override
	public Object parseObject(String source, ParsePosition pos) {//用处不大
		return source;
	}


	private int len(String s) {
		int l = 0;
		for (char c : s.toCharArray()) {
			l += len(c);
		}

		return l;
	}

	private int len(char c) {
		return isSingleByte(c) ? 1 : 2;
	}

	private String sub(String s, int len) {
		StringBuilder sb = new StringBuilder();
		int hasLen = 0;
		for (char c : s.toCharArray()) {
			if (hasLen < len) {
				sb.append(c);
				hasLen += len(c);
			}else{
				break;
			}
		}

		return sb.toString();
	}

	private boolean isSingleByte(char c) {
		return c <= 255;
	}

	public static void main(String[] args) {
		// out.println("房东电话给一还是对方国家偶数个佛山东方宫is东方精工静极思动if个");//随便打的字，只是用来测试
		int colWidth = 51;
		out.println(StrUtil.repeat("=", 25)+"|"+StrUtil.repeat("=", 25));
		out.println();
		StringAlign align = new StringAlign(colWidth, StringAlign.Alignment.CENTER);//调用构造方法，设置字符串对齐为居中对齐，最大长度为50

		out.println(align.format("- i -"));

		out.println(align.format("1010101010101010"));
		out.println(align.format("我是中国gerh人吉林省关edruyhe键是了建设管理局各式"));

	}

}