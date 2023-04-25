package cn.ios.plugin.action;

import cn.ios.casegen.config.Config;
import cn.ios.casegen.config.GlobalCons;
import cn.ios.plugin.ui.ConfigGui;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

public class GenCaseAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        Project project = e.getProject();

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            VirtualFile compilerVirtualFile = ModuleRootManager.getInstance(module).getModifiableModel().getModuleExtension(CompilerModuleExtension.class).getCompilerOutputPath();
            if (compilerVirtualFile != null) {
                GlobalCons.pluginStart = true;
                Config.onceConfig(GlobalCons.LOCAL_JRE_PATH, project.getBasePath(), compilerVirtualFile.getPath(),-1,-1,-1,-1,-1);
            }
        }

        PsiElement element = e.getData(LangDataKeys.PSI_ELEMENT);
        if (element instanceof PsiDirectory) {
            PsiDirectory directory = (PsiDirectory) element;
            PsiPackage pkg = JavaDirectoryService.getInstance().getPackage(directory);
            if (pkg != null) {
                PsiClass[] classes = pkg.getClasses();
                // 对获取到的类进行处理，例如输出到控制台
                for (PsiClass clazz : classes) {
                    GlobalCons.CLASS_NAME_UNDER_TEST.add(clazz.getQualifiedName());
                }
            }
        } else if (element instanceof PsiClass) {
            PsiClass clazz = (PsiClass) element;
            GlobalCons.CLASS_NAME_UNDER_TEST.add(clazz.getQualifiedName());
        }

        // TODO
        GlobalCons.PROJECT_NAME = e.getProject().getName();
        ConfigGui configGui = new ConfigGui();
        configGui.setVisible(true);
    }
}