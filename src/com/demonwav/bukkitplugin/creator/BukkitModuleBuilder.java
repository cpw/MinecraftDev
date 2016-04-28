/*
 * IntelliJ IDEA Bukkit Support Plugin
 *
 * Written by Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.bukkitplugin.creator;

import com.demonwav.bukkitplugin.BukkitModuleType;
import com.demonwav.bukkitplugin.BungeeCordModuleType;
import com.demonwav.bukkitplugin.SpigotModuleType;
import com.demonwav.bukkitplugin.icons.BukkitProjectsIcons;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import javax.swing.Icon;

public class BukkitModuleBuilder extends JavaModuleBuilder {

    private MavenProjectCreator creator = new MavenProjectCreator();

    @Override
    public String getPresentableName() {
        return "Bukkit Plugin";
    }

    @Override
    public Icon getNodeIcon() {
        return BukkitProjectsIcons.BukkitProject;
    }

    @Override
    public String getGroupName() {
        return "Bukkit Plugin";
    }

    @Override
    public int getWeight() {
        return JavaModuleBuilder.BUILD_SYSTEM_WEIGHT - 1;
    }

    @Override
    public boolean isSuitableSdkType(SdkTypeId sdk) {
        return sdk == JavaSdk.getInstance();
    }

    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        final Project project = modifiableRootModel.getProject();
        final VirtualFile root = createAndGetRoot();
        modifiableRootModel.addContentEntry(root);

        if (getModuleJdk() != null)
            modifiableRootModel.setSdk(getModuleJdk());

        creator.setRoot(root);
        creator.setProject(project);

        DumbAwareRunnable r = creator::create;

        if (project.isDisposed()) {
            return;
        }

        if (ApplicationManager.getApplication().isUnitTestMode()
                || ApplicationManager.getApplication().isHeadlessEnvironment()) {
            r.run();
            return;
        }


        if (!project.isInitialized()) {
            StartupManager.getInstance(project).registerPostStartupActivity(r);
            return;
        }

        if (DumbService.isDumbAware(r)) {
            r.run();
        } else {
            DumbService.getInstance(project).runWhenSmart(r);
        }
    }

    private VirtualFile createAndGetRoot() {
        String temp = getContentEntryPath();

        assert temp != null;

        String path = FileUtil.toSystemIndependentName(temp);
        //noinspection ResultOfMethodCallIgnored
        new File(path).mkdirs();
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
    }

    @Override
    public JavaModuleType getModuleType() {
        switch (creator.getType()) {
            case BUKKIT:
                return BukkitModuleType.getInstance();
            case SPIGOT:
                return SpigotModuleType.getInstance();
            case BUNGEECORD:
                return BungeeCordModuleType.getInstance();
            default: // This *should* not happen
                throw new IllegalStateException("Project type is not one of the three possible types.");
        }
    }

    @Override
    public String getParentGroup() {
        return "Bukkit Project";
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[]{
                new MavenWizardStep(creator),
                new ProjectSettingsWizardStep(creator)
        };
    }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return new BukkitWizardStep(creator);
    }

    @Override
    public boolean validate(Project current, Project dest) {
        return true;
    }
}