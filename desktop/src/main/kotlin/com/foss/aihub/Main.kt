package com.foss.aihub.pc

import com.foss.aihub.pc.models.AppSettings
import com.foss.aihub.pc.models.AiService
import com.foss.aihub.pc.models.loadServices
import com.foss.aihub.pc.utils.SettingsManager
import java.awt.*
import java.awt.event.*
import javax.swing.*
import java.io.File

class Main {
    companion object {
        lateinit var settingsManager: SettingsManager
        lateinit var dataDir: File
        lateinit var servicesFile: File
        lateinit var domainsFile: File
        var aiServices: List<AiService> = emptyList()
        var settings = AppSettings()

        fun updateSettings(new: AppSettings) {
            settings = new
            settingsManager.settings = new
        }

        fun updateServices(list: List<AiService>) {
            aiServices = list
        }

        fun saveSettings() {
            settingsManager.settings = settings
        }

        fun refreshServices() {
            aiServices = loadServices(servicesFile)
        }
    }

    @JvmStatic
    fun main() {
        val userHome = System.getProperty("user.home")
        dataDir = File(userHome, ".aihub-pc").apply { mkdirs() }
        servicesFile = File(dataDir, "ais.json")
        domainsFile = File(dataDir, "domains.txt")
        settingsManager = SettingsManager(dataDir)
        settings = settingsManager.settings
        refreshServices()

        SwingUtilities.invokeLater {
            createAndShowGUI()
        }
    }

    private fun createAndShowGUI() {
        val frame = JFrame("AI Hub")
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.setSize(1200, 800)
        frame.location = Point(100, 100)

        val app = AppPanel()
        frame.contentPane.add(app)
        frame.isVisible = true
    }
}

class AppPanel : JPanel(BorderLayout()) {
    private val webView = JEditorPane()
    private val serviceList = DefaultListModel<AiService>()
    private val list = JList(serviceList).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        fixedCellHeight = 40
    }
    private var currentService: AiService? = null

    init {
        webView.isEditable = false
        webView.contentType = "text/html"
        val scrollPane = JScrollPane(webView)

        list.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                val selected = list.selectedValue
                if (selected != null) {
                    currentService = selected
                    loadService(selected)
                    Main.updateSettings(Main.settings.copy(lastOpenedService = selected.name))
                    Main.saveSettings()
                }
            }
        }

        val sidebar = JScrollPane(list).apply {
            preferredSize = Dimension(280, 0)
        }

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, scrollPane).apply {
            dividerLocation = 280
            resizeWeight = 1.0
        }

        add(splitPane, BorderLayout.CENTER)
        add(createMenuBar(), BorderLayout.NORTH)

        // Load last opened service
        val lastOpened = Main.settings.lastOpenedService
        if (lastOpened != null) {
            val service = Main.aiServices.find { it.name == lastOpened } ?: Main.aiServices.firstOrNull()
            service?.let {
                list.selectedValue = it
                loadService(it)
            }
        } else {
            Main.aiServices.firstOrNull()?.let {
                list.selectedIndex = 0
                loadService(it)
            }
        }
    }

    private fun loadService(service: AiService) {
        currentService = service
        SwingUtilities.invokeLater {
            try {
                webView.setPage(service.url)
            } catch (e: Exception) {
                webView.text = "Error loading ${service.name}: ${e.message}"
            }
        }
    }

    private fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()

        val fileMenu = JMenu("File")
        val refreshItem = JMenuItem("Refresh").apply {
            addActionListener { currentService?.let { loadService(it) } }
        }
        val settingsItem = JMenuItem("Settings").apply {
            addActionListener { showSettingsDialog() }
        }
        val exitItem = JMenuItem("Exit").apply {
            addActionListener { System.exit(0) }
        }
        fileMenu.add(refreshItem)
        fileMenu.addSeparator()
        fileMenu.add(settingsItem)
        fileMenu.addSeparator()
        fileMenu.add(exitItem)

        val helpMenu = JMenu("Help")
        val aboutItem = JMenuItem("About").apply {
            addActionListener { showAboutDialog() }
        }
        helpMenu.add(aboutItem)

        menuBar.add(fileMenu)
        menuBar.add(Box.createHorizontalGlue())
        menuBar.add(helpMenu)

        return menuBar
    }

    private fun showSettingsDialog() {
        val dialog = JDialog((SwingUtilities.getWindowAncestor(this) as JFrame), "Settings", true)
        dialog.layout = GridBagLayout()
        val gbc = GridBagConstraints().apply {
            inset = Insets(5, 5, 5, 5)
            anchor = GridBagConstraints.WEST
        }

        val defaultServiceCombo = JComboBox(Main.aiServices.map { it.name }.toTypedArray())
        Main.settings.defaultServiceName?.let { defaultServiceCombo.selectedItem = it }

        val loadLastCheck = JCheckBox("Load last opened AI on startup", Main.settings.loadLastOpenedAI)

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(JLabel("Default AI Service:"), gbc)
        gbc.gridx = 1; gbc.gridy = 0; dialog.add(defaultServiceCombo, gbc)
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; dialog.add(loadLastCheck, gbc)

        val btnPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val okBtn = JButton("OK").apply {
            addActionListener {
                Main.updateSettings(Main.settings.copy(
                    defaultServiceName = defaultServiceCombo.selectedItem as String?,
                    loadLastOpenedAI = loadLastCheck.isSelected
                ))
                Main.saveSettings()
                dialog.dispose()
            }
        }
        val cancelBtn = JButton("Cancel").apply {
            addActionListener { dialog.dispose() }
        }
        btnPanel.add(okBtn)
        btnPanel.add(cancelBtn)

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST; dialog.add(btnPanel, gbc)

        dialog.pack()
        dialog.locationRelativeTo(this)
        dialog.isVisible = true
    }

    private fun showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "AI Hub PC
Version 1.0.0

All your AI assistants, beautifully together.",
            "About AI Hub", JOptionPane.INFORMATION_MESSAGE)
    }
}