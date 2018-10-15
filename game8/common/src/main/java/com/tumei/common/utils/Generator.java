//package com.tumei.common.utils;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.springframework.util.ResourceUtils;
//
//import java.io.*;
//
///**
// * Created by Administrator on 2017/3/22 0022.
// */
//public class Generator {
//	private static Log log = LogFactory.getLog(Generator.class);
//
//	public static void main(String[] args) {
//		try {
//			File file = ResourceUtils.getFile("protocols.json");
//			long l = file.length();
//			if (l > 0) {
//				byte[] bytes = new byte[(int) l];
//				InputStream is = new FileInputStream(file);
//				is.read(bytes);
//				is.close();
//
//				VelocityEngine engine = new VelocityEngine();
//				engine.init();
//				Template protoTemplate = engine.getTemplate("protocol.vm");
//
//				BeansSchema bss = JsonUtil.getMapper().readValue(bytes, BeansSchema.class);
//
//				for (BeanSchema bs : bss.getItems()) {
//					log.info("收入一个:" + bs.getName());
//					VelocityContext ctx = new VelocityContext();
//					ctx.put("module", "xxkg");
//					ctx.put("bs", bs);
//					for (BeanItemSchema bis : bs.getItems()) {
//						if (bis.getKey().equals("int")) {
//							bis.setKey("Integer");
//						}
//						if (bis.getKey().equals("long")) {
//							bis.setKey("Long");
//						}
//						if (bis.getKey().equals("float")) {
//							bis.setKey("Float");
//						}
//
//						if (bis.getValue().equals("int")) {
//							bis.setValue("Integer");
//						}
//						if (bis.getValue().equals("long")) {
//							bis.setValue("Long");
//						}
//						if (bis.getValue().equals("float")) {
//							bis.setValue("Float");
//						}
//					}
//
//					if (bs.isProtocol()) {
//						StringWriter sw = new StringWriter();
//						protoTemplate.merge(ctx, sw);
//						log.info(sw.toString());
//					}
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//}
