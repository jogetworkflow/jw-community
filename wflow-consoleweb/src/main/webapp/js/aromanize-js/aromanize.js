/**
	Aromanize-js
	@author Fajar Chandra
	@since 2017.12.06
        @license MIT
	
	UNICODE TABLE REFERENCES
	Hangul Jamo            0x3131 - 0x
	Hangul Choseong Jaeum  0x1100 - 0x1112
	Hangul Jungseong Moeum 0x1161 - 
	Hangul Jongseong Jaeum 0x11A8
	Hangul Eumjeol         0xAC00
 */
var Aromanize = {

	////////////////////////////////////////////////////////////////////
	// Transliteration rules
	////////////////////////////////////////////////////////////////////
	
	rules: {
		
		hangul: {
			
			/**
			 * Revised Romanization Transcription
			 */
			'rr': {
				// Note: giyeok (0x1100) for middle moeum is different than giyeok (0x3131) for standalone jamo
				cho: {
					'ᄀ': 'g', 'ᄁ': 'kk',
					'ᄂ': 'n',
					'ᄃ': 'd', 'ᄄ': 'tt',
					'ᄅ': 'r', 
					'ᄆ': 'm',
					'ᄇ': 'b', 'ᄈ': 'pp',
					'ᄉ': 's', 'ᄊ': 'ss',
					'ᄋ': '',
					'ᄌ': 'j', 'ᄍ': 'jj',
					'ᄎ': 'ch', 
					'ᄏ': 'k', 
					'ᄐ': 't',
					'ᄑ': 'p',
					'ᄒ': 'h'
				},
				
				// Note: ᅡ (0x1161) for middle moeum is different than ㅏ (0x314F) for standalone jamo
				jung: {
					'ᅡ': 'a', 'ᅢ': 'ae', 'ᅣ': 'ya', 'ᅤ': 'yae', 
					'ᅥ': 'eo', 'ᅦ': 'e', 'ᅧ': 'yeo', 'ᅨ': 'ye', 
					'ᅩ': 'o', 'ᅪ': 'wa', 'ᅫ': 'wae', 'ᅬ': 'oe', 'ᅭ': 'yo',
					'ᅮ': 'u', 'ᅯ': 'wo', 'ᅰ': 'we', 'ᅱ': 'wi', 'ᅲ': 'yu', 
					'ᅳ': 'eu', 'ᅴ': 'eui', 'ᅵ': 'i'
				},
				
				// Note: ᆨ (0x11A8) for last jaeum (batchim) is different than ᄀ (0x1100) for first jaeum
				// also different than ㄱ (0x3131) for standalone jamo
				jong: {
					'ᆨ': 'k', 'ᆨᄋ': 'g', 'ᆨᄂ': 'ngn', 'ᆨᄅ': 'ngn', 'ᆨᄆ': 'ngm', 'ᆨᄒ': 'kh',
					'ᆩ': 'kk', 'ᆩᄋ': 'kg', 'ᆩᄂ': 'ngn', 'ᆩᄅ': 'ngn', 'ᆩᄆ': 'ngm', 'ᆩᄒ': 'kh',
					'ᆪ': 'k', 'ᆪᄋ': 'ks', 'ᆪᄂ': 'ngn', 'ᆪᄅ': 'ngn', 'ᆪᄆ': 'ngm', 'ᆪᄒ': 'kch', 
					'ᆫ': 'n', 'ᆫᄅ': 'll', 
					'ᆬ': 'n', 'ᆬᄋ': 'nj', 'ᆬᄂ': 'nn', 'ᆬᄅ': 'nn', 'ᆬᄆ': 'nm', 'ᆬㅎ': 'nch',
					'ᆭ': 'n', 'ᆭᄋ': 'nh', 'ᆭᄅ': 'nn', 
					'ᆮ': 't', 'ᆮᄋ': 'd', 'ᆮᄂ': 'nn', 'ᆮᄅ': 'nn', 'ᆮᄆ': 'nm', 'ᆮᄒ': 'th', 
					'ᆯ': 'l', 'ᆯᄋ': 'r', 'ᆯᄂ': 'll', 'ᆯᄅ': 'll', 
					'ᆰ': 'k', 'ᆰᄋ': 'lg', 'ᆰᄂ': 'ngn', 'ᆰᄅ': 'ngn', 'ᆰᄆ': 'ngm', 'ᆰᄒ': 'lkh',
					'ᆱ': 'm', 'ᆱᄋ': 'lm', 'ᆱᄂ': 'mn', 'ᆱᄅ': 'mn', 'ᆱᄆ': 'mm', 'ᆱᄒ': 'lmh', 
					'ᆲ': 'p', 'ᆲᄋ': 'lb', 'ᆲᄂ': 'mn', 'ᆲᄅ': 'mn', 'ᆲᄆ': 'mm', 'ᆲᄒ': 'lph', 
					'ᆳ': 't', 'ᆳᄋ': 'ls', 'ᆳᄂ': 'nn', 'ᆳᄅ': 'nn', 'ᆳᄆ': 'nm', 'ᆳᄒ': 'lsh', 
					'ᆴ': 't', 'ᆴᄋ': 'lt', 'ᆴᄂ': 'nn', 'ᆴᄅ': 'nn', 'ᆴᄆ': 'nm', 'ᆴᄒ': 'lth', 
					'ᆵ': 'p', 'ᆵᄋ': 'lp', 'ᆵᄂ': 'mn', 'ᆵᄅ': 'mn', 'ᆵᄆ': 'mm', 'ᆵᄒ': 'lph', 
					'ᆶ': 'l', 'ᆶᄋ': 'lh', 'ᆶᄂ': 'll', 'ᆶᄅ': 'll', 'ᆶᄆ': 'lm', 'ᆶᄒ': 'lh',
					'ᆷ': 'm', 'ᆷᄅ': 'mn', 
					'ᆸ': 'p', 'ᆸᄋ': 'b', 'ᆸᄂ': 'mn', 'ᆸᄅ': 'mn', 'ᆸᄆ': 'mm', 'ᆸᄒ': 'ph', 
					'ᆹ': 'p', 'ᆹᄋ': 'ps', 'ᆹᄂ': 'mn', 'ᆹᄅ': 'mn', 'ᆹᄆ': 'mm', 'ᆹᄒ': 'psh', 
					'ᆺ': 't', 'ᆺᄋ': 's', 'ᆺᄂ': 'nn', 'ᆺᄅ': 'nn', 'ᆺᄆ': 'nm', 'ᆺᄒ': 'sh', 
					'ᆻ': 't', 'ᆻᄋ': 'ss', 'ᆻᄂ': 'tn', 'ᆻᄅ': 'tn', 'ᆻᄆ': 'nm', 'ᆻᄒ': 'th', 
					'ᆼ': 'ng',
					'ᆽ': 't', 'ᆽᄋ': 'j', 'ᆽᄂ': 'nn', 'ᆽᄅ': 'nn', 'ᆽᄆ': 'nm', 'ᆽᄒ': 'ch', 
					'ᆾ': 't', 'ᆾᄋ': 'ch', 'ᆾᄂ': 'nn', 'ᆾᄅ': 'nn', 'ᆾᄆ': 'nm', 'ᆾᄒ': 'ch', 
					'ᆿ': 'k', 'ᆿᄋ': 'k', 'ᆿᄂ': 'ngn', 'ᆿᄅ': 'ngn', 'ᆿᄆ': 'ngm', 'ᆿᄒ': 'kh', 
					'ᇀ': 't', 'ᇀᄋ': 't', 'ᇀᄂ': 'nn', 'ᇀᄅ': 'nn', 'ᇀᄆ': 'nm', 'ᇀᄒ': 'th', 
					'ᇁ': 'p', 'ᇁᄋ': 'p', 'ᇁᄂ': 'mn', 'ᇁᄅ': 'mn', 'ᇁᄆ': 'mm', 'ᇁᄒ': 'ph', 
					'ᇂ': 't', 'ᇂᄋ': 'h', 'ᇂᄂ': 'nn', 'ᇂᄅ': 'nn', 'ᇂᄆ': 'mm', 'ᇂᄒ': 't',
					'ᇂᄀ': 'k', 
				}
			},
			
			/**
			 * Revised Romanization Transliteration
			 */
			'rr-translit': {
				// Note: giyeok (0x1100) for middle moeum is different than giyeok (0x3131) for standalone jamo
				cho: {
					'ᄀ': 'g', 'ᄁ': 'kk',
					'ᄂ': 'n',
					'ᄃ': 'd', 'ᄄ': 'tt',
					'ᄅ': 'l', 
					'ᄆ': 'm',
					'ᄇ': 'b', 'ᄈ': 'pp',
					'ᄉ': 's', 'ᄊ': 'ss',
					'ᄋ': '',
					'ᄌ': 'j', 'ᄍ': 'jj',
					'ᄎ': 'ch', 
					'ᄏ': 'k', 
					'ᄐ': 't',
					'ᄑ': 'p',
					'ᄒ': 'h'
				},		
				
				// Note: ᅡ (0x1161) for middle moeum is different than ㅏ (0x314F) for standalone jamo
				jung: {
					'ᅡ': 'a', 'ᅢ': 'ae', 'ᅣ': 'ya', 'ᅤ': 'yae', 
					'ᅥ': 'eo', 'ᅦ': 'e', 'ᅧ': 'yeo', 'ᅨ': 'ye', 
					'ᅩ': 'o', 'ᅪ': 'oa', 'ᅫ': 'oae', 'ᅬ': 'oi', 'ᅭ': 'yo',
					'ᅮ': 'u', 'ᅯ': 'ueo', 'ᅰ': 'ue', 'ᅱ': 'ui', 'ᅲ': 'yu', 
					'ᅳ': 'eu', 'ᅴ': 'eui', 'ᅵ': 'i'
				},
				
				// Note: ᆨ (0x11A8) for last jaeum (batchim) is different than ᄀ (0x1100) for first jaeum
				// also different than ㄱ (0x3131) for standalone jamo
				jong: {
					'ᆨ': 'g', 'ᆨᄋ': 'g-',
					'ᆩ': 'kk', 'ᆩᄋ': 'kk-',
					'ᆪ': 'gs', 'ᆪᄋ': 'gs-', 'ᆪᄉ': 'gs-s', 
					'ᆫ': 'n', 'ᆫᄋ': 'n-', 
					'ᆬ': 'nj', 'ᆬᄋ': 'nj-', 'ᆬᄌ': 'nj-j', 
					'ᆭ': 'nh', 'ᆭᄋ': 'nh-',
					'ᆮ': 'd', 'ᆮᄋ': 'd-',
					'ᆯ': 'l', 'ᆯᄋ': 'l-',
					'ᆰ': 'lg', 'ᆰᄋ': 'lg-', 
					'ᆱ': 'lm', 'ᆱᄋ': 'lm-', 
					'ᆲ': 'lb', 'ᆲᄋ': 'lb-', 
					'ᆳ': 'ls', 'ᆳᄋ': 'ls-', 'ᆳᄉ': 'ls-s', 
					'ᆴ': 'lt', 'ᆴᄋ': 'lt-', 
					'ᆵ': 'lp', 'ᆵᄋ': 'lp-', 
					'ᆶ': 'lh', 'ᆶᄋ': 'lh-', 
					'ᆷ': 'm', 'ᆷᄋ': 'm-', 
					'ᆸ': 'b', 'ᆸᄋ': 'b-', 
					'ᆹ': 'bs', 'ᆹᄋ': 'bs-', 'ᆹᄉ': 'bs-s', 
					'ᆺ': 's', 'ᆺᄋ': 's-', 'ᆺᄊ': 's-ss', 
					'ᆻ': 'ss', 'ᆻᄋ': 'ss-', 'ᆻᄉ': 'ss-s', 
					'ᆼ': 'ng', 'ᆼᄋ': 'ng-',
					'ᆽ': 'j', 'ᆽᄋ': 'j-', 'ᆽᄌ': 'j-j', 
					'ᆾ': 'ch', 'ᆾᄋ': 'ch-', 
					'ᆿ': 'k', 'ᆿᄋ': 'k-', 
					'ᇀ': 't', 'ᇀᄋ': 't-', 
					'ᇁ': 'p', 'ᇁᄋ': 'p-', 
					'ᇂ': 'h', 'ᇂᄋ': 'h-'
				}
			},
			
			'skats': {
				hyphen: ' ',
				
				// Note: giyeok (0x1100) for middle moeum is different than giyeok (0x3131) for standalone jamo
				cho: {
					'ᄀ': 'L', 'ᄁ': 'LL',
					'ᄂ': 'F',
					'ᄃ': 'B', 'ᄄ': 'BB',
					'ᄅ': 'V', 
					'ᄆ': 'M',
					'ᄇ': 'W', 'ᄈ': 'WW',
					'ᄉ': 'G', 'ᄊ': 'GG',
					'ᄋ': 'K',
					'ᄌ': 'P', 'ᄍ': 'PP',
					'ᄎ': 'C', 
					'ᄏ': 'X', 
					'ᄐ': 'Z',
					'ᄑ': 'O',
					'ᄒ': 'J',
					' ': '  '
				},		
				
				// Note: ᅡ (0x1161) for middle moeum is different than ㅏ (0x314F) for standalone jamo
				jung: {
					'ᅡ': 'E', 'ᅢ': 'EU', 'ᅣ': 'I', 'ᅤ': 'IU', 
					'ᅥ': 'T', 'ᅦ': 'TU', 'ᅧ': 'S', 'ᅨ': 'SU', 
					'ᅩ': 'A', 'ᅪ': 'AE', 'ᅫ': 'AEU', 'ᅬ': 'AU', 'ᅭ': 'N',
					'ᅮ': 'H', 'ᅯ': 'HT', 'ᅰ': 'HTU', 'ᅱ': 'HU', 'ᅲ': 'R', 
					'ᅳ': 'D', 'ᅴ': 'DU', 'ᅵ': 'U'
				},
				
				// Note: ᆨ (0x11A8) for last jaeum (batchim) is different than ᄀ (0x1100) for first jaeum
				// also different than ㄱ (0x3131) for standalone jamo
				jong: {
					'ᆨ': 'L', 'ᆩ': 'LL', 'ᆪ': 'LG', 
					'ᆫ': 'F', 'ᆬ': 'FP', 'ᆭ': 'FJ', 
					'ᆮ': 'B', 
					'ᆯ': 'V', 'ᆰ': 'VL', 'ᆱ': 'VM', 'ᆲ': 'VW', 'ᆳ': 'VG', 'ᆴ': 'VZ', 'ᆵ': 'VO', 'ᆶ': 'VJ', 
					'ᆷ': 'M', 
					'ᆸ': 'W', 'ᆹ': 'WG', 
					'ᆺ': 'G', 'ᆻ': 'GG', 
					'ᆼ': 'K', 
					'ᆽ': 'P', 
					'ᆾ': 'C', 
					'ᆿ': 'X', 
					'ᇀ': 'Z', 
					'ᇁ': 'O', 
					'ᇂ': 'J'
				}
			},
			
			/**
			 * Indonesian Transcription
			 */
			'ebi': {
				// Note: giyeok (0x1100) for middle moeum is different than giyeok (0x3131) for standalone jamo
				cho: {
					'ᄀ': 'gh', 'ᄁ': 'k',
					'ᄂ': 'n',
					'ᄃ': 'dh', 'ᄄ': 't',
					'ᄅ': 'r', 
					'ᄆ': 'm',
					'ᄇ': 'b', 'ᄈ': 'p',
					'ᄉ': 's', 'ᄊ': 's',
					'ᄋ': '',
					'ᄌ': 'jh', 'ᄍ': 'c',
					'ᄎ': 'ch', 
					'ᄏ': 'kh', 
					'ᄐ': 'th',
					'ᄑ': 'ph',
					'ᄒ': 'h'
				},
				
				// Note: giyeok (0x1100) for middle moeum is different than giyeok (0x3131) for standalone jamo
				cho2: {
					'ᄀ': 'g', 'ᄁ': 'k',
					'ᄂ': 'n',
					'ᄃ': 'd', 'ᄄ': 't',
					'ᄅ': 'r', 
					'ᄆ': 'm',
					'ᄇ': 'b', 'ᄈ': 'p',
					'ᄉ': 's', 'ᄊ': 's',
					'ᄋ': '',
					'ᄌ': 'j', 'ᄍ': 'c',
					'ᄎ': 'ch', 
					'ᄏ': 'kh', 
					'ᄐ': 'th',
					'ᄑ': 'ph',
					'ᄒ': 'h'
				},
				
				// Note: ᅡ (0x1161) for middle moeum is different than ㅏ (0x314F) for standalone jamo
				jung: {
					'ᅡ': 'a', 'ᅢ': 'è', 'ᅣ': 'ya', 'ᅤ': 'yè', 
					'ᅥ': 'ö', 'ᅦ': 'é', 'ᅧ': 'yö', 'ᅨ': 'yé', 
					'ᅩ': 'o', 'ᅪ': 'wa', 'ᅫ': 'wè', 'ᅬ': 'wé', 'ᅭ': 'yo',
					'ᅮ': 'u', 'ᅯ': 'wo', 'ᅰ': 'wé', 'ᅱ': 'wi', 'ᅲ': 'yu', 
					'ᅳ': 'eu', 'ᅴ': 'eui', 'ᅵ': 'i'
				},
				
				// Note: ᆨ (0x11A8) for last jaeum (batchim) is different than ᄀ (0x1100) for first jaeum
				// also different than ㄱ (0x3131) for standalone jamo
				jong: {
					'ᆨ': 'k', 'ᆨᄋ': 'g', 'ᆨᄂ': 'ngn', 'ᆨᄅ': 'ngn', 'ᆨᄆ': 'ngm', 'ᆨᄒ': 'kh',
					'ᆩ': 'k', 'ᆩᄋ': 'kg', 'ᆩᄂ': 'ngn', 'ᆩᄅ': 'ngn', 'ᆩᄆ': 'ngm', 'ᆩᄒ': 'kh',
					'ᆪ': 'k', 'ᆪᄋ': 'ks', 'ᆪᄂ': 'ngn', 'ᆪᄅ': 'ngn', 'ᆪᄆ': 'ngm', 'ᆪᄒ': 'kch', 
					'ᆫ': 'n', 'ᆫᄅ': 'll', 
					'ᆬ': 'n', 'ᆬᄋ': 'nj', 'ᆬᄂ': 'nn', 'ᆬᄅ': 'nn', 'ᆬᄆ': 'nm', 'ᆬㅎ': 'nch',
					'ᆭ': 'n', 'ᆭᄋ': 'nh', 'ᆭᄅ': 'nn', 
					'ᆮ': 't', 'ᆮᄋ': 'd', 'ᆮᄂ': 'nn', 'ᆮᄅ': 'nn', 'ᆮᄆ': 'nm', 'ᆮᄒ': 'th', 
					'ᆯ': 'l', 'ᆯᄋ': 'r', 'ᆯᄂ': 'll', 'ᆯᄅ': 'll', 
					'ᆰ': 'k', 'ᆰᄋ': 'lg', 'ᆰᄂ': 'ngn', 'ᆰᄅ': 'ngn', 'ᆰᄆ': 'ngm', 'ᆰᄒ': 'lkh',
					'ᆱ': 'm', 'ᆱᄋ': 'lm', 'ᆱᄂ': 'mn', 'ᆱᄅ': 'mn', 'ᆱᄆ': 'mm', 'ᆱᄒ': 'lmh', 
					'ᆲ': 'p', 'ᆲᄋ': 'lb', 'ᆲᄂ': 'mn', 'ᆲᄅ': 'mn', 'ᆲᄆ': 'mm', 'ᆲᄒ': 'lph', 
					'ᆳ': 't', 'ᆳᄋ': 'ls', 'ᆳᄂ': 'nn', 'ᆳᄅ': 'nn', 'ᆳᄆ': 'nm', 'ᆳᄒ': 'lsh', 
					'ᆴ': 't', 'ᆴᄋ': 'lt', 'ᆴᄂ': 'nn', 'ᆴᄅ': 'nn', 'ᆴᄆ': 'nm', 'ᆴᄒ': 'lth', 
					'ᆵ': 'p', 'ᆵᄋ': 'lp', 'ᆵᄂ': 'mn', 'ᆵᄅ': 'mn', 'ᆵᄆ': 'mm', 'ᆵᄒ': 'lph', 
					'ᆶ': 'l', 'ᆶᄋ': 'lh', 'ᆶᄂ': 'll', 'ᆶᄅ': 'll', 'ᆶᄆ': 'lm', 'ᆶᄒ': 'lh',
					'ᆷ': 'm', 'ᆷᄅ': 'mn', 
					'ᆸ': 'p', 'ᆸᄋ': 'b', 'ᆸᄂ': 'mn', 'ᆸᄅ': 'mn', 'ᆸᄆ': 'mm', 'ᆸᄒ': 'ph', 
					'ᆹ': 'p', 'ᆹᄋ': 'ps', 'ᆹᄂ': 'mn', 'ᆹᄅ': 'mn', 'ᆹᄆ': 'mm', 'ᆹᄒ': 'psh', 
					'ᆺ': 't', 'ᆺᄋ': 'sh', 'ᆺᄂ': 'nn', 'ᆺᄅ': 'nn', 'ᆺᄆ': 'nm', 'ᆺᄒ': 'sh', 
					'ᆻ': 't', 'ᆻᄋ': 's', 'ᆻᄂ': 'nn', 'ᆻᄅ': 'nn', 'ᆻᄆ': 'nm', 'ᆻᄒ': 'th', 
					'ᆼ': 'ng',
					'ᆽ': 't', 'ᆽᄋ': 'j', 'ᆽᄂ': 'nn', 'ᆽᄅ': 'nn', 'ᆽᄆ': 'nm', 'ᆽᄒ': 'ch', 
					'ᆾ': 't', 'ᆾᄋ': 'ch', 'ᆾᄂ': 'nn', 'ᆾᄅ': 'nn', 'ᆾᄆ': 'nm', 'ᆾᄒ': 'ch', 
					'ᆿ': 'k', 'ᆿᄋ': 'k', 'ᆿᄂ': 'ngn', 'ᆿᄅ': 'ngn', 'ᆿᄆ': 'ngm', 'ᆿᄒ': 'kh', 
					'ᇀ': 't', 'ᇀᄋ': 't', 'ᇀᄂ': 'nn', 'ᇀᄅ': 'nn', 'ᇀᄆ': 'nm', 'ᇀᄒ': 'th', 'ᇀ이': 'ch',
					'ᇁ': 'p', 'ᇁᄋ': 'p', 'ᇁᄂ': 'mn', 'ᇁᄅ': 'mn', 'ᇁᄆ': 'mm', 'ᇁᄒ': 'ph', 
					'ᇂ': 't', 'ᇂᄋ': 'h', 'ᇂᄂ': 'nn', 'ᇂᄅ': 'nn', 'ᇂᄆ': 'mm', 'ᇂᄒ': 't', 
					'ᇂᄀ': 'kh', 'ᇂᄃ': 'dh', 'ᇂᄇ': 'bh'
				}
			},
			
			/**
			 * Kontsevich
			 */
			'konsevich': {
				// Note: giyeok (0x1100) for middle moeum is different than giyeok (0x3131) for standalone jamo
				cho: {
					'ᄀ': 'к', 'ᄁ': 'кк',
					'ᄂ': 'н',
					'ᄃ': 'т', 'ᄄ': 'тт',
					'ᄅ': 'р', 
					'ᄆ': 'м',
					'ᄇ': 'п', 'ᄈ': 'пп',
					'ᄉ': 'с', 'ᄊ': 'сс',
					'ᄋ': '',
					'ᄌ': 'ч', 'ᄍ': 'чч',
					'ᄎ': 'чх', 
					'ᄏ': 'кх', 
					'ᄐ': 'тх',
					'ᄑ': 'пх',
					'ᄒ': 'х'
				},
				
				// Note: ᅡ (0x1161) for middle moeum is different than ㅏ (0x314F) for standalone jamo
				jung: {
					'ᅡ': 'а', 'ᅢ': 'э', 'ᅣ': 'я', 'ᅤ': 'йя', 
					'ᅥ': 'о', 'ᅦ': 'е́', 'ᅧ': 'ё', 'ᅨ': 'йе', 
					'ᅩ': 'о́', 'ᅪ': 'ва', 'ᅫ': 'вэ', 'ᅬ': 'ве', 'ᅭ': 'ё',
					'ᅮ': 'у', 'ᅯ': 'во', 'ᅰ': 'ве', 'ᅱ': 'ви', 'ᅲ': 'ю', 
					'ᅳ': 'ы', 'ᅴ': 'ый', 'ᅵ': 'и'
				},
				
				// Note: ᆨ (0x11A8) for last jaeum (batchim) is different than ᄀ (0x1100) for first jaeum
				// also different than ㄱ (0x3131) for standalone jamo
				jong: {
					'ᆨ': 'к', 'ᆨᄋ': 'г', 'ᆨᄂ': 'нн', 'ᆨᄅ': 'нн', 'ᆨᄆ': 'нм', 
					'ᆩ': 'кк', 'ᆩᄋ': 'кк', 'ᆩᄂ': 'нн', 'ᆩᄅ': 'нн', 'ᆩᄆ': 'нм',
					'ᆪ': 'к', 'ᆪᄋ': 'кс', 'ᆪᄂ': 'нн', 'ᆪᄅ': 'нн', 'ᆪᄆ': 'нм', 'ᆪᄒ': 'ксх', 
					'ᆫ': 'н', 'ᆫᄀ': 'нг', 'ᆫᄃ': 'нд', 'ᆫᄅ': 'лл', 'ᆫᄇ': 'нб', 'ᆫᄌ': 'ндж', 
					'ᆬ': 'н', 'ᆬᄋ': 'нч', 'ᆬᄂ': 'нн', 'ᆬᄅ': 'нн', 'ᆬᄆ': 'нм', 'ᆬㅎ': 'нчх',
					'ᆭ': 'н', 'ᆭᄋ': 'нх', 'ᆭᄅ': 'нн', 
					'ᆮ': 'т', 'ᆮᄂ': 'тн', 'ᆮᄅ': 'нн', 'ᆮᄆ': 'нм', 
					'ᆯ': 'ль', 'ᆯᄋ': 'р', 'ᆯᄀ': 'льг', 'ᆯᄂ': 'лл', 'ᆯᄃ': 'льтт', 'ᆯᄅ': 'лл', 'ᆯᄇ': 'льб', 'ᆯᄉ': 'льсс', 'ᆯᄌ': 'льчч', 'ᆯᄒ': 'рх', 
					'ᆰ': 'к', 'ᆰᄋ': 'льг', 'ᆰᄀ': 'льг', 'ᆰᄂ': 'нн', 'ᆰᄅ': 'нн', 'ᆰᄆ': 'нм', 'ᆰᄒ': 'лькх',
					'ᆱ': 'м', 'ᆱᄋ': 'льм', 'ᆱᄂ': 'мн', 'ᆱᄅ': 'мн', 'ᆱᄆ': 'мм', 'ᆱᄒ': 'льмх', 
					'ᆲ': 'п', 'ᆲᄋ': 'льп', 'ᆲᄂ': 'мн', 'ᆲᄅ': 'мн', 'ᆲᄆ': 'мм', 'ᆲᄒ': 'льпх', 
					'ᆳ': 'т', 'ᆳᄋ': 'льс', 'ᆳᄂ': 'нн', 'ᆳᄅ': 'нн', 'ᆳᄆ': 'мн', 'ᆳᄒ': 'льс', 
					'ᆴ': 'т', 'ᆴᄋ': 'льтх', 'ᆴᄂ': 'нн', 'ᆴᄅ': 'нн', 'ᆴᄆ': 'нм', 'ᆴᄒ': 'льтх', 
					'ᆵ': 'п', 'ᆵᄋ': 'льпх', 'ᆵᄂ': 'мн', 'ᆵᄅ': 'мн', 'ᆵᄆ': 'мм', 'ᆵᄒ': 'льпх', 
					'ᆶ': 'ль', 'ᆶᄋ': 'льх', 'ᆶᄂ': 'лл', 'ᆶᄅ': 'лл', 'ᆶᄆ': 'льм', 'ᆶᄒ': 'льх',
					'ᆷ': 'м', 'ᆷᄀ': 'мг', 'ᆷᄃ': 'мд', 'ᆷᄅ': 'мн', 'ᆷᄇ': 'мб', 'ᆷᄌ': 'мдж', 
					'ᆸ': 'п', 'ᆸᄋ': 'б', 'ᆸᄂ': 'мн', 'ᆸᄅ': 'мн', 'ᆸᄆ': 'мм', 
					'ᆹ': 'п', 'ᆹᄋ': 'пс', 'ᆹᄂ': 'мн', 'ᆹᄅ': 'мн', 'ᆹᄆ': 'мм', 
					'ᆺ': 'т', 'ᆺᄋ': 'с', 'ᆺᄂ': 'нн', 'ᆺᄅ': 'нн', 'ᆺᄆ': 'нм', 'ᆺᄒ': 'с', 
					'ᆻ': 'т', 'ᆻᄋ': 'сс', 'ᆻᄅ': 'тн', 'ᆻᄆ': 'нм', 'ᆻᄒ': 'тх', 
					'ᆼ': 'нъ', 'ᆼᄀ': 'нг', 'ᆼᄃ': 'нд', 'ᆼᄅ': 'нн', 'ᆼᄇ': 'нб', 'ᆼᄌ': 'ндж',
					'ᆽ': 'т', 'ᆽᄋ': 'ч', 'ᆽᄂ': 'нн', 'ᆽᄅ': 'нн', 'ᆽᄆ': 'нм', 'ᆽᄒ': 'чх', 
					'ᆾ': 'т', 'ᆾᄋ': 'чх', 'ᆾᄂ': 'нн', 'ᆾᄅ': 'нн', 'ᆾᄆ': 'нм', 'ᆾᄒ': 'чх', 
					'ᆿ': 'к', 'ᆿᄋ': 'кх', 'ᆿᄂ': 'нн', 'ᆿᄅ': 'нн', 'ᆿᄆ': 'нм', 'ᆿᄒ': 'кх', 
					'ᇀ': 'т', 'ᇀᄋ': 'тх', 'ᇀᄂ': 'нн', 'ᇀᄅ': 'нн', 'ᇀᄆ': 'нм', 'ᇀᄒ': 'тх', 
					'ᇁ': 'п', 'ᇁᄋ': 'пх', 'ᇁᄂ': 'мн', 'ᇁᄅ': 'мн', 'ᇁᄆ': 'мм', 'ᇁᄒ': 'пх', 
					'ᇂ': 'т', 'ᇂᄋ': 'х', 'ᇂᄂ': 'нн', 'ᇂᄅ': 'нн', 'ᇂᄆ': 'мм', 'ᇂᄒ': 'тх',
					'ᇂᄀ': 'кх', 
				}
			}
			
		}
	},
	
	////////////////////////////////////////////////////////////////////
	// Conversion methods
	////////////////////////////////////////////////////////////////////

	/**
	 * Converts Hangul to Romaja
	 * 
	 * Options/Parameters:
	 * text      - (String) Source string.
	 * rule      - (String) Romanization rule.
	 *             Possible values: rr|rr-translit|skats
	 * hyphen    - (String) Hyphenate syllables with specified characters.
	 * 
	 * Return:
	 * (String) Romanized string.
	 */
	hangulToLatin: function() { // (text, rule, hyphen)
		
		// Helper functions
		// Check if it's letter or numbers
		var isChoseong = function(char) {
			if(char.charCodeAt(0) >= 0x1100 && char.charCodeAt(0) <= 0x1112) {
				return true;
			}
			else {
				return false;
			}
		};
		
		// Options mapping
		var args = {};
		if(typeof arguments[0] == 'object') {
			args = arguments[0];
		}
		else {
			args.text = arguments[0];
			args.rule = arguments[1];
			args.hyphen = arguments[2];
		}
		
		if(args.hyphen == null) {
			args.hyphen = '';
		}
		
		var rulemap = this.rules.hangul.rr;
		if(args.rule != null && this.rules.hangul[args.rule] != null) {
			rulemap = this.rules.hangul[args.rule];
		}
		else if(args.rule != null) {
			throw 'Invalid rule ' + args.rule;
		}
		
		var rom = '';
		var curr = null, next;
		var skipJaeum = false; // Indicates jaeum of current iteration to be skipped
		for(var i = 0; i <= args.text.length; i++) {
			// If next is hangul syllable, separate it into jamo
			// 0xAC00 is the first hangul syllable in unicode table
			// 0x1100 is the first hangul jaeum in unicode table
			// 0x1161 is the first hangul moeum in unicode table
			// 0x11A8 is the first hangul batchim in unicode table
			nextIdx = args.text.charCodeAt(i) - 0xAC00;
			if(!isNaN(nextIdx) && nextIdx >= 0 && nextIdx <= 11171) {
				next = String.fromCharCode(Math.floor(nextIdx / 588) + 0x1100)
					+ String.fromCharCode(Math.floor(nextIdx % 588 / 28) + 0x1161)
					+ (nextIdx % 28 == 0 ? '' : String.fromCharCode(nextIdx % 28 + 0x11A7)); // Index 0 is reserved for nothing
			}
			else {
				next = args.text.charAt(i);
			}
			
			// Except for first iteration (curr is null), 
			// Curr and next contains 2 or 3 jamo, or 1 non-hangul letter
			if(curr != null) {
				
				var res = '';
				
				// Choseong Jaeum
				if(!skipJaeum) {
					// If not the first syllable, try cho2 if defined
					if(i > 0 && !/\s/.test(args.text.charAt(i-2)) && 
					   rulemap.cho2 != undefined &&
					   rulemap.cho2[curr.charAt(0)] != undefined
					) {
						res += rulemap.cho2[curr.charAt(0)];
					}
					else if(rulemap.cho[curr.charAt(0)] != undefined) {
						res += rulemap.cho[curr.charAt(0)];
					}
					else {
						res += curr.charAt(0);
					}
				}
				skipJaeum = false;
				
				// Jungseong Moeum
				if(curr.length > 1) {
					if(rulemap.jung[curr.charAt(1)] != undefined) {
						res += rulemap.jung[curr.charAt(1)];
					}
					else {
						res += curr.charAt(1);
					}
					
					// Add hyphen if no batchim
					if(curr.length == 2 && isChoseong(next.charAt(0))) {
						res += ' ';
					}
				}
				
				// Jongseong Jaeum (Batchim)
				if(curr.length > 2) {
					// Changing sound with next jaeum + moeum
					if(rulemap.jong[curr.charAt(2) + next.charAt(0) + next.charAt(1)] != undefined) {
						res += rulemap.jong[curr.charAt(2) + next.charAt(0) + next.charAt(1)];
						skipJaeum = true;
						
						// No need to add hyphen here as it's already defined
					}
					// Changing sound with next jaeum
					else if(rulemap.jong[curr.charAt(2) + next.charAt(0)] != undefined) {
						res += rulemap.jong[curr.charAt(2) + next.charAt(0)];
						skipJaeum = true;
						
						// No need to add hyphen here as it's already defined
					}
					// Unchanging sound
					else if(rulemap.jong[curr.charAt(2)] != undefined) {
						res += rulemap.jong[curr.charAt(2)];
						
						// Add hyphen
						if(isChoseong(next.charAt(0))) {
							res += ' ';
						}
					}
					else {
						res += curr.charAt(2);
						
						// Add hyphen
						if(isChoseong(next.charAt(0))) {
							res += ' ';
						}
					}
				}
				
				// Replace hyphen (if this is hangeul word)
				if(curr.length > 1) {
					if(args.hyphen == '' && rulemap.hyphen != null) {
						res = res.replace(' ', rulemap.hyphen);
					}
					else {
						// Soft hyphen
						res = res.replace(' ', args.hyphen);
						// Hard hyphen
						if(args.hyphen != '') {
							res = res.replace('-', args.hyphen);
						}
					}
				}
				rom += res;
			}
			
			curr = next;
		}
		return rom;
	},
	
	////////////////////////////////////////////////////////////////////
	// All-in-one converters
	////////////////////////////////////////////////////////////////////

	/**
	 * Converts Hangul/Hiragana/Katakana to Romaja
	 * 
	 * Conversion is done using default conversion rule for each script.
	 * If you wish to specify which rule to use, use hangulToLatin(), 
	 * hiraganaToLatin(), or katakanaToLatin() function.
	 * 
	 * Options/Parameters:
	 * text      - (String) Source text.
	 * rule      - (String) Romanization rule.
	 * hyphen    - (String) Hyphenate syllables with specified characters.
	 * 
	 * Return:
	 * (String) Romanized string.
	 */
	toLatin: function() { // (text, rule, hyphen)
		return this.hangulToLatin.apply(this, arguments);
	},

	/**
	 * Converts Hangul/Hiragana/Katakana to Romaja
	 * 
	 * This is an alias of toRomaja().
	 */
	romanize: function() { 
		return this.toLatin.apply(this, arguments); 
	},

	/**
	 * Converts Romaji/Hangul/Katakana to Hiragana
	 */
	toHiragana: function(text) {
		//TODO
		throw 'Not implemented';
		return text;
	},

	/**
	 * Converts Romaji/Hangul/Hiragana to Katakana
	 */
	toKatakana: function(text) {
		//TODO
		throw 'Not implemented';
		return text;
	},

	/**
	 * Converts Romaji/Hiragana/Katakana to Hangul
	 */
	toHangul: function(text) {
		//TODO
		throw 'Not implemented';
		return text;
	},

};

////////////////////////////////////////////////////////////////////
// String extensions
////////////////////////////////////////////////////////////////////

if((typeof AROMANIZE_EXTEND_STRING == 'undefined' || AROMANIZE_EXTEND_STRING) &&
   (typeof document == 'undefined' || /\?(.+&)?base(=true)?(&.+)?$/.test(document.currentScript.src) == false)
  ) {
		
	// romanize()
	if(typeof String.prototype.romanize == 'undefined') {
		String.prototype.romanize = function() {
			var args = Array.prototype.slice.call(arguments);
			args.unshift(this.toString());
			return Aromanize.toLatin.apply(Aromanize, args);
		};
	}

}

////////////////////////////////////////////////////////////////////////////////
// Export Node.js module
////////////////////////////////////////////////////////////////////////////////

if(typeof module != 'undefined') {
	module.exports = Aromanize;
}

////////////////////////////////////////////////////////////////////////////////
// Command line interface
////////////////////////////////////////////////////////////////////////////////

if(typeof process != 'undefined' && require.main == module) {
	
	// Capture options
	var script = Aromanize.toLatin;
	var options = {};
	for(var i = 2; i < process.argv.length; i++) {
		// Script
		switch(process.argv[i]) {
			case '-r':
			case '--romanize':
			case '-l':
			case '--latin':
				script = Aromanize.toLatin;
				break;
				
			case '-h':
			case '--hangul':
				script = Aromanize.toHangul;
				break;
				
			case '-i':
			case '--hiragana':
				script = Aromanize.toHiragana;
				break;
				
			case '-k':
			case '--katakana':
				script = Aromanize.toKatakana;
				break;
			case '-c':
			case '--cyrillic':
				script = Aromanize.toLatin;
				options.rule = 'konsevich';
				break;
		}
		
		// Options
		if(process.argv[i][0] == '-') {
			var opt = process.argv[i].split('=');
			switch(opt[0]) {
				default:
					if(opt[0].length > 2) {
						options[opt[0].substr(2)] = opt[1];
					}
			}
		}
		
		// Input
		else {
			options['text'] = process.argv[i];
		}
	}
	
	// If no input provided or --help is triggered, show help
	if(options['text'] == null || options['--help'] != undefined) {
		console.log('\n\
Usage:\n\
  aromanize [TARGET] [OPTIONS] <input>\n\
\n\
Example:\n\
  aromanize -r "안녕하세요?"\n\
\n\
TARGET:\n\
  -r, --romanize,   \n\
  -l, --latin       Converts to Latin script (Romaja).\n\
  -c, --cyrillic    Converts to Cyrillic script.\n\
\n\
OPTIONS:\n\
      --rule=RULE   Use specified transliteration rule.\n\
      --help        Display this help message.\n\
      \n\
RULE:\n\
  rr                Revised Romanization Transcription (default for -r)\n\
  rr-translit       Revised Romanization Transliteration\n\
  skats             SKATS Coding\n\
  ebi               Indonesian Transcription\n\
  konsevich         Konsevich (default for -c)\n\
		');
		process.exit(0);
	}
	
	// Execute script
	//console.log(options);
	console.log(script.call(Aromanize, options));
}
