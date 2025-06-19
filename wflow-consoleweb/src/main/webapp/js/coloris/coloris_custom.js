(function(window, document, Math, undefined) {
    'use strict';

    // Constants for localStorage keys and limits
    const STORAGE_KEY = 'Coloris_favoriteColors'; // Key for storing favorite colors
    const RECENT_KEY = 'Coloris_recentColors';   // Key for storing recent colors
    const MAX_RECENT = 6;                        // Maximum number of recent colors
    const MAX_ATTEMPTS = 50;                     // Maximum attempts to initialize the color picker
    const MAX_RENDER_ATTEMPTS = 5;               // Maximum attempts to render swatches

    // Global state
    let currentInstance = null;                   // Tracks the currently active input element
    let initAttempts = 0;                        // Counter for initialization attempts
    let favoriteColors = (JSON.parse(localStorage.getItem(STORAGE_KEY)) || []).map(normalizeColorToHex); // Load and normalize favorite colors
    let lastPickedColor = null;                   // Stores the last picked color

    // === Color Utility Functions ===

    /**
     * Converts a color string to an RGBA array.
     * @param {string} color - The color value (e.g., '#FF0000', 'red', 'rgba(255, 0, 0, 1)').
     * @returns {number[]} Array of [R, G, B, A] values.
     */
    function colorToRgbaArray(color) {
        const div = document.createElement("div");
        div.style.color = color;
        div.style.display = 'none';
        document.body.appendChild(div);

        const computed = getComputedStyle(div).color;
        document.body.removeChild(div);

        const match = computed.match(/rgba?\((\d+), (\d+), (\d+)(?:, ([\d.]+))?\)/);
        if (!match) return [0, 0, 0, 1]; // Fallback to black with full opacity

        const r = parseInt(match[1], 10);
        const g = parseInt(match[2], 10);
        const b = parseInt(match[3], 10);
        const a = match[4] !== undefined ? parseFloat(match[4]) : 1;

        return [r, g, b, a];
    }

    /**
     * Checks if two colors are visually similar based on RGB and alpha differences.
     * @param {string} c1 - First color.
     * @param {string} c2 - Second color.
     * @param {number} tolerance - Maximum RGB distance for similarity (default: 10).
     * @param {number} alphaTolerance - Maximum alpha difference (default: 0.05).
     * @returns {boolean} True if colors are similar.
     */
    function areColorsVisuallySimilar(c1, c2, tolerance = 10, alphaTolerance = 0.05) {
        const [r1, g1, b1, a1] = colorToRgbaArray(c1);
        const [r2, g2, b2, a2] = colorToRgbaArray(c2);

        const colorDistance = Math.sqrt(
            (r1 - r2) ** 2 +
            (g1 - g2) ** 2 +
            (b1 - b2) ** 2
        );

        const alphaDiff = Math.abs(a1 - a2);
        return colorDistance <= tolerance && alphaDiff <= alphaTolerance;
    }

    /**
     * Normalizes a color to a hexadecimal format (e.g., #RRGGBB or #RRGGBBAA).
     * @param {string} color - The color to normalize.
     * @returns {string} Hexadecimal color string.
     */
    function normalizeColorToHex(color) {
        try {
            const [r, g, b, a] = colorToRgbaArray(color);
            const toHex = (val) => val.toString(16).padStart(2, '0');
            const hex = `#${toHex(r)}${toHex(g)}${toHex(b)}`;
            return a < 1 ? `${hex}${toHex(Math.round(a * 255))}` : hex;
        } catch (err) {
            return color; // fallback: return original input unchanged
        }
    }

    // === LocalStorage Management ===

    /**
     * Loads recent colors from localStorage, normalized to hex format.
     * @returns {string[]} Array of recent color hex codes.
     */
    function loadRecentColors() {
        const recent = localStorage.getItem(RECENT_KEY);
        return (recent ? JSON.parse(recent) : []).map(normalizeColorToHex).slice(0, MAX_RECENT);
    }

    /**
     * Saves a color to the recent colors list in localStorage, avoiding duplicates.
     * @param {string} color - The color to save.
     */
    function saveRecentColors(color) {
        const hex = normalizeColorToHex(color);
        let recentColors = loadRecentColors();

        const isDuplicate = recentColors.some(c => areColorsVisuallySimilar(c, color));
        if (!isDuplicate) {
            if (recentColors.length >= MAX_RECENT) {
                recentColors.shift(); // Remove oldest color if limit reached
            }
            recentColors.push(hex);
            localStorage.setItem(RECENT_KEY, JSON.stringify(recentColors));
            renderSwatches();
        }
    }

    /**
     * Saves a color to the favorite colors list in localStorage, avoiding duplicates.
     * @param {string} color - The color to save.
     */
    function saveColor(color) {
        const hexColor = normalizeColorToHex(color);
        if (!favoriteColors.some(existing => areColorsVisuallySimilar(existing, color))) {
            favoriteColors.push(hexColor);
            localStorage.setItem(STORAGE_KEY, JSON.stringify(favoriteColors));
            renderSwatches();
        } else {
            alert("This color is already in your favorites.");
        }
    }

    /**
     * Deletes a color from the favorite colors list in localStorage.
     * @param {string} color - The color to remove.
     */
    function deleteFavoriteColor(color) {
        const picker = document.querySelector('.clr-picker');
        if (!picker || !picker.classList.contains('clr-open')) {
            return;
        }
        favoriteColors = JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];
        const index = favoriteColors.indexOf(color);
        if (index !== -1) {
            favoriteColors.splice(index, 1);
            localStorage.setItem(STORAGE_KEY, JSON.stringify(favoriteColors));
            renderSwatches();
        }
    }

    // === UI Components ===

    /**
     * Opens a modal for configuring the custom color palette via JSON input.
     */
    function openPaletteSettings() {
        const modal = document.createElement('div');
        modal.className = 'clr-palette-settings-modal';
        modal.innerHTML =
        '<div class="clr-palette-settings-content">' +
            '<h3>Custom Palette Settings</h3>' +
            '<p>Enter a JSON array of color values (e.g., <code>["#FF0000", "#00FF00"]</code>):</p>' +
            '<textarea id="clr-palette-input" rows="5"></textarea>' +
            '<div class="clr-palette-actions">' +
                '<button id="clr-palette-save">Save</button>' +
                '<button id="clr-palette-cancel">Cancel</button>' +
            '</div>' +
        '</div>';


        document.body.appendChild(modal);

        // Prevent color picker from closing while modal is open
        const picker = document.querySelector('.clr-picker');
        if (picker) {
            picker.setAttribute('data-prevent-close', 'true');
        }

        const saveBtn = modal.querySelector('#clr-palette-save');
        const cancelBtn = modal.querySelector('#clr-palette-cancel');
        const input = modal.querySelector('#clr-palette-input');

        saveBtn.onclick = () => {
            try {
                const value = input.value.trim();
                if (!value) {
                    alert('Please enter a valid JSON array.');
                    return;
                }
                const parsed = JSON.parse(value);
                if (!Array.isArray(parsed)) {
                    alert('Input must be a JSON array.');
                    return;
                }
                localStorage.setItem('Coloris_customPalette', JSON.stringify(parsed));
                alert('Custom palette saved successfully!');
                document.body.removeChild(modal);
                if (picker) picker.removeAttribute('data-prevent-close');
                renderSwatches();
            } catch (e) {
                alert('Invalid JSON: ' + e.message);
            }
        };

        cancelBtn.onclick = () => {
            document.body.removeChild(modal);
            if (picker) picker.removeAttribute('data-prevent-close');
        };
    }

    /**
     * Renders the color swatches (palette, favorite, and recent colors) in the picker.
     * Expose function to window, so it can be used to update different predefined color properties 
    */
    window.renderSwatches = function renderSwatches() {
        let attempts = 0;
        const attemptRender = () => {
            const picker = document.querySelector('.clr-picker');
            const swatchesContainer = document.getElementById("clr-swatches");
            if (!picker || !swatchesContainer || attempts >= MAX_RENDER_ATTEMPTS) {
                if (attempts < MAX_RENDER_ATTEMPTS) {
                    attempts++;
                    setTimeout(attemptRender, 200); // Retry after delay
                }
                return;
            }

            swatchesContainer.innerHTML = ''; // Clear existing content

            const swatchesWrapper = document.createElement('div');

            // Load custom palette or use default
            let paletteColors = [];
            const customPalette = localStorage.getItem('Coloris_customPalette');
            if (customPalette) {
                try {
                    paletteColors = JSON.parse(customPalette);
                } catch (e) {
                    console.error('Error parsing custom palette:', e);
                }
            }
            if (paletteColors.length === 0) {
                paletteColors = [
                                    '#1677ff',
                                    '#722ed1', 
                                    '#52c41a', 
                                    '#fadb14', 
                                    '#f5222d', 
                                    '#000000' 
                                ];
            }

            // Render palette colors
            paletteColors.forEach((color, i) => {
                const button = document.createElement('button');
                button.type = 'button';
                button.id = `clr-swatch-${i}`;
                button.setAttribute('aria-labelledby', `clr-swatch-label clr-swatch-${i}`);
                button.setAttribute('data-favorite-color', color.toLowerCase());
                button.style.color = color;
                button.textContent = color;

                button.addEventListener('click', () => {
                    const activeInput = currentInstance || document.querySelector('#clr-color-value');
                    if (activeInput && activeInput.value !== color) {
                        activeInput.value = color;
                        activeInput.dispatchEvent(new Event('input'));
                    }
                });

                swatchesWrapper.appendChild(button);
            });

            // Render favorite colors
            favoriteColors.forEach((color, i) => {
                const wrapper = document.createElement('div');
                wrapper.className = 'clr-swatch-fav-wrapper';

                const button = document.createElement('button');
                button.type = 'button';
                button.id = `clr-swatch-fav-${i}`;
                button.setAttribute('aria-labelledby', `clr-swatch-label clr-swatch-fav-${i}`);
                button.setAttribute('data-favorite-color', color.toLowerCase());
                button.style.color = color;
                button.textContent = color;

                const deleteButton = document.createElement('span');
                deleteButton.className = 'clr-delete-fav';
                deleteButton.innerHTML = '<i class="fas fa-trash-alt"></i>';
                deleteButton.setAttribute('aria-label', `Remove favorite color ${color}`);
                deleteButton.addEventListener('click', () => deleteFavoriteColor(color));

                button.addEventListener('click', () => {
                    const activeInput = currentInstance || document.querySelector('#clr-color-value');
                    if (activeInput && activeInput.value !== color) {
                        activeInput.value = color;
                        activeInput.dispatchEvent(new Event('input'));
                    }
                });

                wrapper.appendChild(button);
                wrapper.appendChild(deleteButton);
                swatchesWrapper.appendChild(wrapper);
            });

            swatchesContainer.appendChild(swatchesWrapper);

            // Render recent colors
            const recentColors = loadRecentColors();
            if (recentColors.length > 0) {
                const recentContainer = document.createElement('div');
                recentContainer.className = 'clr-recent';
                const recentLabel = document.createElement('div');
                recentLabel.className = 'clr-swatches-label';
                recentLabel.setAttribute('aria-label', 'Recent colors section');
                recentLabel.textContent = 'Recent Colors';
                recentContainer.appendChild(recentLabel);
                const recentSwatchesWrapper = document.createElement('div');
                recentSwatchesWrapper.className = 'clr-swatch-buttons';
                recentContainer.appendChild(recentSwatchesWrapper);

                recentColors.forEach(color => {
                    const item = document.createElement('button');
                    item.className = 'clr-color-item';
                    item.style.color = color;
                    item.textContent = color;
                    item.setAttribute('data-color', color);
                    item.setAttribute('aria-label', `Recent color ${color}`);

                    item.addEventListener('click', () => {
                        const activeInput = currentInstance || document.querySelector('#clr-color-value');
                        if (activeInput && activeInput.value !== color) {
                            activeInput.value = color;
                            activeInput.dispatchEvent(new Event('input'));
                        }
                    });

                    recentSwatchesWrapper.appendChild(item);
                });

                swatchesContainer.appendChild(recentContainer);
            }
        };

        attemptRender();
    }

    /**
     * Checks if the browser supports the EyeDropper API.
     * @returns {boolean} True if EyeDropper is supported.
     */
    function hasEyeDropperSupport() {
        return 'EyeDropper' in window;
    }

    /**
     * Initializes the eyedropper button for screen color picking.
     */
    function initEyedropper() {
        const picker = document.querySelector('.clr-picker');
        if (!picker) return;

        const existingEyedropper = picker.querySelector('.clr-eyedropper');
        if (existingEyedropper) existingEyedropper.remove();

        const hueAlphaContainer = document.createElement('div');
        hueAlphaContainer.className = 'clr-hue-alpha-container';

        const hue = picker.querySelector('.clr-hue');
        const alpha = picker.querySelector('.clr-alpha');
        if (hue && alpha && hue.parentNode === alpha.parentNode) {
            hue.parentNode.insertBefore(hueAlphaContainer, hue);
            hueAlphaContainer.appendChild(hue);
            hueAlphaContainer.appendChild(alpha);
        }

        const eyeDropperButton = document.createElement('div');
        eyeDropperButton.className = 'clr-eyedropper';
        eyeDropperButton.innerHTML = '<i class="zmdi zmdi-eyedropper"></i>';
        eyeDropperButton.setAttribute('aria-label', 'Pick color from screen');

        hueAlphaContainer.parentNode.insertBefore(eyeDropperButton, hueAlphaContainer);

        if (!hasEyeDropperSupport()) {
            eyeDropperButton.setAttribute('title', 'Not supported');
            eyeDropperButton.setAttribute('disabled', 'true');

            eyeDropperButton.style.opacity = '0.5';
            eyeDropperButton.style.cursor = 'not-allowed'; 
            return;
        }

        eyeDropperButton.addEventListener('click', async () => {
            try {
                const eyeDropper = new EyeDropper();
                const result = await eyeDropper.open();
                const selectedColor = result.sRGBHex;

                const input = document.querySelector('#clr-color-value');
                const preview = document.querySelector('.clr-preview');
                if (input) {
                    input.value = selectedColor;
                    lastPickedColor = selectedColor;
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                    input.dispatchEvent(new Event('change', { bubbles: true }));
                }
                if (preview) {
                    preview.style.backgroundColor = selectedColor;
                }

                setTimeout(() => {
                    if (input) {
                        input.focus();
                        input.click();
                    }
                }, 0);
            } catch (error) {
                console.error('EyeDropper error:', error.message);
            }
        });
    }

    /**
     * Adds a "Save" button to add the current color to favorites.
     */
    function addSaveButton() {
        const picker = document.querySelector('.clr-picker');
        if (!picker) return;

        const existingSaveBtn = picker.querySelector('.clr-save-btn');
        if (existingSaveBtn) existingSaveBtn.remove();

        let buttonRow = picker.querySelector('.clr-button-row');
        if (!buttonRow) {
            buttonRow = document.createElement('div');
            buttonRow.className = 'clr-button-row';
        }

        const saveBtn = document.createElement('button');
        saveBtn.className = 'clr-save-btn';
        saveBtn.textContent = 'Save';
        saveBtn.setAttribute('aria-label', 'Save current color to favorites');

        const clearBtn = picker.querySelector('.clr-clear');
        if (clearBtn && clearBtn.parentNode) {
            clearBtn.parentNode.removeChild(clearBtn);
            buttonRow.appendChild(saveBtn);
            buttonRow.appendChild(clearBtn);
        } else {
            buttonRow.appendChild(saveBtn);
        }

        if (!picker.contains(buttonRow)) {
            picker.appendChild(buttonRow);
        }

        saveBtn.onclick = () => {
            const picker = document.querySelector('.clr-picker');
            if (!picker || !picker.classList.contains('clr-open')) return;

            let currentColor = '';
            const colorValue = picker.querySelector('#clr-color-value');
            currentColor = colorValue ? colorValue.value : '';
            if (!currentColor) {
                const preview = picker.querySelector('.clr-preview');
                currentColor = preview ? preview.style.backgroundColor : '';
            }
            if (currentColor) {
                saveColor(currentColor);
            }
        };
    }

    /**
     * Adds a settings button to open the palette configuration modal.
     */
    function addSettingsButton() {
        const picker = document.querySelector('.clr-picker');
        const existingGearBtn = picker.querySelector('.clr-settings-btn');
        if (!existingGearBtn) {
            const gearBtn = document.createElement('div');
            gearBtn.className = 'clr-settings-btn';
            gearBtn.innerHTML = '<i class="zmdi zmdi-settings"></i>';
            gearBtn.setAttribute('aria-label', 'Configure palette');

            gearBtn.onclick = openPaletteSettings;
            picker.appendChild(gearBtn);
        }
    }

    /**
     * Overrides the default clear button behavior to clear the input without closing the picker.
     */
    function overrideClearButtonBehavior() {
        const clearBtn = document.querySelector('.clr-picker .clr-clear');
        if (!clearBtn) return;

        const clonedBtn = clearBtn.cloneNode(true);
        clonedBtn.style.display = 'inline-block';

        clonedBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            const input = document.querySelector('#clr-color-value');
            if (input) {
                input.value = '';
                input.dispatchEvent(new Event('input', { bubbles: true }));
                input.dispatchEvent(new Event('change', { bubbles: true }));
            }

            //Clear the input field
            if (currentInstance) {
                currentInstance.value = '';
            }
        });

        clearBtn.replaceWith(clonedBtn);
    }

    /**
     * Updates the input field with the latest normalized color value.
     */
    function updateLatestColor() {
        const input = currentInstance || document.querySelector('#clr-color-value');
        const preview = document.querySelector('.clr-preview');

        let latestColor = input && input.value ? input.value : (preview && preview.style.backgroundColor ? preview.style.backgroundColor : null);
        if (input && latestColor) {
            const normalized = normalizeColorToHex(latestColor);
            if (input.value !== normalized) {
                input.value = normalized;
            }
        }
    }

    /**
     * Initializes custom features (eyedropper, save button, settings button, clear button).
     */
    function initCustomFeatures() {
        initEyedropper();
        addSaveButton();
        addSettingsButton();
        overrideClearButtonBehavior();
    }

    /**
     * Checks if the color picker exists and initializes custom features.
     * @returns {boolean} True if initialized successfully.
     */
    function checkAndInit() {
        const picker = document.querySelector('.clr-picker');
        if (picker) {
            initCustomFeatures();
            return true;
        }
        return false;
    }

    /**
     * Observes DOM changes to handle color picker open/close and update swatches/recent colors.
     */
    function startObserving() {
        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                    const picker = mutation.target;
                    if (picker && picker.classList && !picker.classList.contains('clr-open')) {
                        renderSwatches();
                        const input = currentInstance || document.querySelector('#clr-color-value');
                        if (input) {
                            input.removeEventListener('input', updateLatestColor);
                            input.removeEventListener('change', updateLatestColor);
                        }
                        if (lastPickedColor) {
                            saveRecentColors(lastPickedColor);
                        }
                        lastPickedColor = null;
                    }
                }
            });
        });

        if (document.body) {
            observer.observe(document.body, {
                childList: true,
                subtree: true,
                attributes: true
            });
        }
    }

    /**
     * Initializes event listeners for color picking and modal interaction.
     */
    function initEventListeners() {
        document.addEventListener('coloris:pick', e => {
            currentInstance = e.detail.currentEl;
            const input = document.querySelector('#clr-color-value');
            if (input && e.detail && e.detail.color) {
                lastPickedColor = e.detail.color;
            }
            if (input) {
                input.addEventListener('input', updateLatestColor);
                input.addEventListener('change', updateLatestColor);
            }
        });

        // Disable/Enable recent colors based on recent color property
        //and reset positioning so it doesnt overlap
        $(document).on('click', '.clr-field > input', function () {
            var obj = this;
            
            if ($(this).parent().hasClass('allowRecentColors')) {
                $("div#clr-picker").addClass("allow-recent-colors");
            } else {
                $("div#clr-picker").removeClass("allow-recent-colors");
            }

            $("div#clr-picker").css({'visibility' : 'hidden', 'position' : 'fixed'});
            setTimeout(function(){
                const windowHeight = $(window).outerHeight();
                const clrPicker = $("div#clr-picker");
                const clrPickerHeight = clrPicker.outerHeight();
                const cpOffset = clrPicker.offset();
                const inputOffset = $(obj).offset();
                const aboveInput = cpOffset.top < inputOffset.top;

                function placeBeside(inputOffset, obj, clrPicker) {
                    const objOffset = $(obj).offset();
                    const objWidth = $(obj).outerWidth();
                    const pickerWidth = clrPicker.outerWidth();
                    const windowWidth = $(window).width();

                    let leftPos = $("body").hasClass("rtl")
                        ? objOffset.left - pickerWidth - 5
                        : objOffset.left + objWidth + 5;

                    if (leftPos + pickerWidth > windowWidth) {
                        leftPos = objOffset.left - pickerWidth - 5;
                    }

                    if (leftPos < 0) {
                        leftPos = objOffset.left + objWidth + 5;
                    }

                    clrPicker.css({
                        top: inputOffset.top - (0.5 * clrPicker.outerHeight()),
                        left: leftPos
                    });
                }

                // If it's on top of the input
                if (aboveInput && Math.ceil(cpOffset.top) < 0) {
                    if (inputOffset.top + $(obj).outerHeight() + clrPickerHeight < windowHeight) {
                        clrPicker.css({ top: inputOffset.top + $(obj).outerHeight() });
                    } else {
                        placeBeside(inputOffset, obj, clrPicker);
                    }
                } else if (!aboveInput && Math.ceil(cpOffset.top) + clrPickerHeight > $(window).outerHeight()) {
                    if (inputOffset.top - clrPickerHeight > 0) {
                        clrPicker.css({ top: inputOffset.top - $(clrPicker).outerHeight()});
                    } else {
                        placeBeside(inputOffset, obj, clrPicker);
                    }
                }
        
                $("div#clr-picker").css({'visibility' : 'visible'})
            }, 200)
        });

        // Keeps the color picker open when clicking inside the modal or input field
        document.addEventListener('mousedown', (event) => {
            const modal = document.querySelector('.clr-palette-settings-modal');
            const picker = document.querySelector('.clr-picker');

            if (modal) {
                if (modal.contains(event.target) && event.target.id !== 'clr-palette-input') {
                    event.stopPropagation();
                    event.preventDefault();
                    return;
                }
                if (event.target.id === 'clr-palette-input') {
                    event.stopPropagation();
                    return;
                }
            }

            if (picker && picker.hasAttribute('data-prevent-close')) {
                if (!modal?.contains(event.target) && !picker.contains(event.target)) {
                    event.stopPropagation();
                    event.preventDefault();
                }
            }
        }, true);
    }

    /**
     * Initializes the color picker, setting up observers and event listeners.
     */
    function init() {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => {
                startObserving();
                initEventListeners();
                if (!checkAndInit()) {
                    const initInterval = setInterval(() => {
                        if (checkAndInit() || ++initAttempts >= MAX_ATTEMPTS) {
                            clearInterval(initInterval);
                        }
                    }, 100);
                }
            });
        } else {
            startObserving();
            initEventListeners();
            if (!checkAndInit()) {
                const initInterval = setInterval(() => {
                    if (checkAndInit() || ++initAttempts >= MAX_ATTEMPTS) {
                        clearInterval(initInterval);
                    }
                }, 100);
            }
        }
    }

    // Start the initialization process
    init();
})(window, document, Math, undefined);