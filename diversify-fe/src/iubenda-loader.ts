/**
 * Carica lo script iubenda per i link Privacy Policy e Cookie Policy del footer.
 * Il caricamento dopo l'evento load mantiene il comportamento precedente e
 * assicura che i componenti Angular abbiano già renderizzato i link nel DOM.
 */
const loadIubendaScript = (): void => {
  const script = document.createElement('script');
  const firstScript = document.getElementsByTagName('script')[0];

  script.src = 'https://cdn.iubenda.com/iubenda.js';

  if (firstScript?.parentNode) {
    firstScript.parentNode.insertBefore(script, firstScript);
  } else {
    document.head.appendChild(script);
  }
};

if (document.readyState === 'complete') {
  loadIubendaScript();
} else {
  window.addEventListener('load', loadIubendaScript, { once: true });
}
