/**
 * MediationCompare class.
 * 
 * This class provides functionalities for the mediation comparison UI. It offers features such as:
 * - Syncing list group item heights: Ensures that list group items in different cards have consistent heights.
 * - Hover highlights: Highlights corresponding list group items in all cards when one is hovered over.
 * - Drag-scrolling: Enables users to click and drag to scroll horizontally within the container.
 * - Tooltips: Displays a tooltip with additional information when hovering over a list group item.
 * 
 * The class also uses the `currentPage` and `ruleCode` from the configuration for navigation purposes.
 * 
 * @class
 * @param {HTMLElement} container - The main container element for the mediation compare UI.
 * @param {Object} config - Configuration object for the class.
 * @param {boolean} config.autoHeight - Flag to sync list group item heights.
 * @param {boolean} config.hoverHighlight - Flag to activate hover highlights.
 * @param {boolean} config.dragScroll - Flag to activate drag scrolling.
 * @param {boolean} config.tooltips - Flag to activate tooltips.
 * @param {string} config.merge - Flag to merge capabilities.
 */
export default class MediationCompare {
    constructor(container, config) {
        this.container = container;
        this.idendityCards = this.container.querySelectorAll('.card');
        this.mergeForm = document.getElementById('mediation-merge-form')
        this.tooltip = null;
        this.config = config;
        this.init();
    }
    /**
     * Initializes the MediationCompare instance.
     * Waits for document fonts to be ready before activating features based on the provided configuration.
     * @private
     */
    init() {
        document.fonts.ready.then(() => {
            if (this.config.autoHeight) {
                this.syncListGroupItemHeights();
            }
            if (this.config.hoverHighlight) {
                this.activateHoverHighlight();
            }
            if (this.config.dragScroll) {
                this.activateDragScroll();
            }
            if (this.config.tooltips) {
                this.activateTooltips();
            }
            if (this.config.merge) {
                this.activateMerge();
            }
            this.copyTooltipContent();
        });
    }
    /**
     * Attaches event listeners to merge buttons within the container.
     * When a merge button is clicked, it rotates the button, toggles its styling, 
     * and triggers the merge action based on its rotation state.
     *
     */
    activateMerge() {
        const mergeBtns = this.container.querySelectorAll('.mediation-btn-merge');
        mergeBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                let icon = btn.querySelector('i.ti');
                let mediationLine = btn.closest('.list-group-item').querySelector('.mediation-line-merge');
                let attrLineLeft = this.container.querySelector('.lutece-compare-item-container.rounded-start-5 li.list-group-item[data-key="' + btn.dataset.key + '"]');
                let attrLineRight = this.container.querySelector('.lutece-compare-item-container.rounded-end-5 li.list-group-item[data-key="' + btn.dataset.key + '"]');
                if (icon.classList.contains('ti-arrow-left')) {
                    btn.classList.add('bg-warning-subtle', 'text-warning-emphasis', 'border-warning');
                    btn.classList.remove('border-primary-subtle', 'btn-light');
                    icon.classList.remove('ti-arrow-left');
                    icon.classList.add('ti-x');
                    mediationLine.classList.add('border-warning-subtle');
                    mediationLine.classList.remove('border-primary-subtle');
                    attrLineLeft.classList.add('text-decoration-line-through', 'opacity-25');
                    attrLineRight.classList.add('bg-warning-subtle');
                    this.merge(btn);
                } else {
                    btn.classList.remove('bg-warning-subtle', 'text-warning-emphasis');
                    btn.classList.add('border-primary-subtle', 'btn-light');
                    icon.classList.remove('ti-x');
                    icon.classList.add('ti-arrow-left');
                    mediationLine.classList.remove('border-warning-subtle');
                    mediationLine.classList.add('border-primary-subtle');
                    attrLineLeft.classList.remove('text-decoration-line-through', 'opacity-25');
                    attrLineRight.classList.remove('bg-warning-subtle');
                    this.merge(btn, false);
                }
            });
        });
    }
    /**
   * Creates or removes hidden input fields in the container based on the provided merge button's dataset.
   * When merging, new input fields are created and appended to the container.
   * When un-merging, relevant input fields are removed from the container.
   *
   * @param {HTMLElement} btn - The merge button element containing dataset values.
   * @param {boolean} [isMerge=true] - Flag to indicate whether to merge or un-merge.
   */
    merge(btn, isMerge = true) {
        const { key, value, certif, timestampCertif } = btn.dataset;
        const inputValues = [value, certif, timestampCertif];
        const inputNames = [
            `override-${key}`,
            `override-${key}-certif`,
            `override-${key}-timestamp-certif`
        ];
        const recapAttrList = document.getElementById('recap-attributes-merge-ul');
        if (isMerge) {
            inputNames.forEach((name, index) => {
                const newInput = document.createElement('input');
                newInput.name = name;
                newInput.classList.add("d-none");
                newInput.value = inputValues[index];
                newInput.disabled = false;
                this.mergeForm.appendChild(newInput);
            });
            const recap = document.createElement('li');
            recap.id = `recap-override-${key}`;
            recap.innerHTML = `<b>${btn.dataset.name}</b> : ${value} (${certif} - ${btn.dataset.certifdate})`;
            recapAttrList.appendChild(recap);
            recapAttrList.parentElement.classList.remove('d-none');
        } else {
            inputNames.forEach((name) => {
                const formChildren = Array.from(this.mergeForm.children);
                for (let formChild of formChildren) {
                    if (formChild.getAttribute("name") === name) {
                        this.mergeForm.removeChild(formChild);
                    }
                }
            });
            recapAttrList.removeChild(document.getElementById(`recap-override-${key}`));
            if (recapAttrList.childNodes.length === 0) {
                recapAttrList.parentElement.classList.add('d-none');
            }
        }
    }
    /**
     * Synchronizes the heights of list group items across all cards.
     * Ensures that list group items in different cards have consistent heights.
     * @private
     */
    syncListGroupItemHeights() {
        let maxHeights = [];
        this.idendityCards.forEach(card => {
            const items = card.querySelectorAll('.list-group-item');
            items.forEach((item, index) => {
                if (!maxHeights[index] || item.offsetHeight > maxHeights[index]) {
                    maxHeights[index] = item.offsetHeight;
                }
            });
        });
        this.idendityCards.forEach(card => {
            const items = card.querySelectorAll('.list-group-item');
            items.forEach((item, index) => {
                item.style.height = maxHeights[index] + 'px';
            });
        });
    }
    /**
     * Activates the hover highlight feature.
     * Highlights corresponding list group items in all cards when one is hovered over.
     * @private
     */
    activateHoverHighlight() {
        this.idendityCards.forEach(card => {
            const items = card.querySelectorAll('.list-group-item');
            items.forEach((item, index) => {
                item.addEventListener('mouseover', () => {
                    this.idendityCards.forEach((innerCard, cardIndex) => {
                        const innerItems = innerCard.querySelectorAll('.list-group-item');
                        if (innerItems[index]) {
                            innerItems[index].classList.add('bg-primary-subtle');
                        }
                    });
                });
                item.addEventListener('mouseout', () => {
                    this.idendityCards.forEach(innerCard => {
                        const innerItems = innerCard.querySelectorAll('.list-group-item');
                        if (innerItems[index]) {
                            innerItems[index].classList.remove('bg-primary-subtle');
                        }
                    });
                });
            });
        });
    }
    /**
     * Activates the drag scroll feature.
     * Allows users to click and drag to scroll horizontally within the container.
     * @private
     */
    activateDragScroll() {
        let isDown = false;
        let startX;
        let scrollLeft;
        this.container.addEventListener('mousedown', (e) => {
            isDown = true;
            this.container.style.cursor = 'grabbing';
            this.container.style.userSelect = 'none';
            startX = e.pageX - this.container.offsetLeft;
            scrollLeft = this.container.scrollLeft;
        });
        this.container.addEventListener('mouseleave', () => {
            isDown = false;
            this.container.style.cursor = 'grab';
            this.container.style.userSelect = '';
        });
        this.container.addEventListener('mouseup', () => {
            isDown = false;
            this.container.style.cursor = 'grab';
            this.container.style.userSelect = '';
        });
        this.container.addEventListener('mousemove', (e) => {
            if (!isDown) return;
            e.preventDefault();
            const x = e.pageX - this.container.offsetLeft;
            const walk = (x - startX);
            this.container.scrollLeft = scrollLeft - walk;
        });
        this.container.style.cursor = 'grab';
    }
    /**
     * Activates the tooltips feature.
     * Displays a tooltip with additional information when hovering over a list group item.
     * @private
     */
    activateTooltips() {
        this.tooltip = document.createElement('div');
        this.tooltip.style.pointerEvents = 'none';
        this.tooltip.style.zIndex = '1000';
        this.tooltip.style.display = 'none';
        this.tooltip.classList.add('bg-dark', 'position-absolute', 'text-white', 'px-4', 'py-2', 'border', 'border-white', 'border-2', 'rounded-start-5', 'shadow-lg', 'rounded-end-2');
        document.body.appendChild(this.tooltip);
        const items = this.container.querySelectorAll('.list-group-item');
        items.forEach(item => {
            item.addEventListener('mouseover', (event) => {
                const containerLeftPosition = this.container.getBoundingClientRect().left;
                this.tooltip.style.left = containerLeftPosition + 'px';
                this.tooltip.style.top = event.pageY + 'px';
                this.tooltip.style.display = 'block';
                this.tooltip.innerText = item.dataset.name;
            });
            item.addEventListener('mousemove', (event) => {
                const containerLeftPosition = this.container.getBoundingClientRect().left;
                this.tooltip.style.left = containerLeftPosition - this.tooltip.offsetWidth + 12 + 'px';
                this.tooltip.style.top = (event.pageY) - 15 + 'px';
            });
            item.addEventListener('mouseout', () => {
                this.tooltip.style.display = 'none';
            });
        });
    }

    copyTooltipContent() {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('.copy-tooltip'));
        const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
        tooltipTriggerList.forEach(function(element) {
            element.addEventListener('click', function() {
                const customerId = this.getAttribute('data-customer-id');
    
                navigator.clipboard.writeText(customerId).then(() => {
                    const tooltip = bootstrap.Tooltip.getInstance(this);
                    const originalTitle = this.getAttribute('data-bs-original-title');
                    tooltip.setContent({ '.tooltip-inner': 'Copié' });
                    setTimeout(() => {
                        tooltip.setContent({ '.tooltip-inner': originalTitle });
                    }, 300);
                }).catch(err => {
                    console.error('Erreur lors de la copie : ', err);
                });
            });
        });
    }
}