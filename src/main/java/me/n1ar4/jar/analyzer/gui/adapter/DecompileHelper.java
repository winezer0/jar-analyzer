/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.engine.index.IndexPluginsSupport;
import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.utils.JarUtil;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DecompileHelper {
    public static void decompile(TreePath selPath) {
        String sel = selPath.toString();
        sel = sel.substring(1, sel.length() - 1);
        String[] selArray = sel.split(",");
        ArrayList<String> pathList = new ArrayList<>();
        for (String s : selArray) {
            s = s.trim();
            pathList.add(s);
        }

        String[] path = pathList.toArray(new String[0]);
        String filePath = String.join(File.separator, path);

        Path thePath = Paths.get(filePath);

        if (JarUtil.isConfigFile(filePath)) {
            try {
                MainForm.getCodeArea().setText(new String(Files.readAllBytes(thePath)));
                MainForm.getCodeArea().setCaretPosition(0);
            } catch (Exception ignored) {
            }
            return;
        }

        if (!filePath.endsWith(".class")) {
            return;
        }

        if (!Files.exists(thePath)) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "file not exist");
            return;
        }

        // LUCENE 索引处理
        if (LuceneSearchForm.getInstance() != null && LuceneSearchForm.usePaLucene()) {
            IndexPluginsSupport.addIndex(thePath.toFile());
        }
        String code = DecompileEngine.decompile(thePath);

        // SET FILE TREE HIGHLIGHT

        MainForm.getCodeArea().setText(code);
        MainForm.getCodeArea().setCaretPosition(0);

        StringBuilder classNameBuilder = new StringBuilder();
        for (int i = 1; i < path.length; i++) {
            classNameBuilder.append(path[i]).append("/");
        }
        String className = classNameBuilder.toString();
        int i = className.indexOf("classes");

        if (className.contains("BOOT-INF") || className.contains("WEB-INF")) {
            className = className.substring(i + 8, className.length() - 7);
        } else {
            className = className.substring(0, className.length() - 7);
        }

        CoreHelper.refreshAllMethods(className);

        MainForm.setCurClass(className);

        MainForm.getInstance().getCurClassText().setText(className);
        String jarName = MainForm.getEngine().getJarByClass(className);
        MainForm.getInstance().getCurJarText().setText(jarName);
        MainForm.getInstance().getCurMethodText().setText(null);
        MainForm.setCurMethod(null);
    }
}
