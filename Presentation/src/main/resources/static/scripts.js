function initialize()
{
	setupCallBacks();
	setDatePickerDefaults();
	loadAirportCodes();
	initTable();
}

function setupCallBacks()
{
    var tripTypes = $('#tripTypes');
    var origin = $('#origin');
    var destination = $('#destination');
    var departureDate = $('#departureDate');
    var returnDate = $('#returnDate');
	tripTypes.change(function() {
		toggledTripType();
    });
	origin.change(function() {
		onChangeOrigin(origin);
    });
    origin.focus(function() {
		origin.select();
	});
	destination.change(function() {
		onChangeDestination(destination);
    });
    destination.focus(function() {
		destination.select();
	});
	departureDate.change(function() {
		onChangeDepartureDate(departureDate);
    });
	returnDate.change(function() {
		onChangeReturnDate(returnDate);
    });
}

function onChangeOrigin(field)
{
	var value = field.val();
	if( value != null && value.length >= 3 )
	{
		field.val( value.substring(0, 3).toUpperCase() );
	}
	refreshResults();
}

function onChangeDestination(field)
{
	var value = field.val();
	if( value != null && value.length >= 3 )
	{
		field.val( value.substring(0, 3).toUpperCase() );
	}
	refreshResults();
}

function onChangeDepartureDate(field)
{
	if( $('#returnDate').is(":visible") )
	{
		//This will result in a call to refreshResults():
		adjustMinimumReturnDate();
	}
	else
	{
		refreshResults();
	}
}

function adjustMinimumReturnDate()
{
    var departureDate = getDate( $('#departureDate') );
    var returnDate = getDate( $('#returnDate') );
    if( departureDate != null )
    {
		var nextDay = departureDate;
		nextDay.setDate( nextDay.getDate() + 1 );
		$('#returnDate').datepicker("setStartDate", nextDay);
		if( returnDate != null && returnDate <= departureDate )
		{
			$('#returnDate').datepicker("setDate", nextDay);
		}
    }
}

function onChangeReturnDate(field)
{
	refreshResults();
}

function getDate(field)
{
	return field.datepicker('getDate');
}

function setDatePickerDefaults()
{
    $.fn.datepicker.defaults.format = "MM dd, yyyy";
    $.fn.datepicker.defaults.autoclose = true;
    $.fn.datepicker.defaults.todayBtn = false;
    $.fn.datepicker.defaults.todayHighlight = false;
    $.fn.datepicker.defaults.startDate = getTomorrow();
    $('#departureDate').datepicker("setDate", getTomorrow());
}

function getTomorrow()
{
	var date = new Date();
	date.setDate( date.getDate() + 1 );
	return date;
}

function toggledTripType()
{
	var oneway = $('#oneway').is(':checked');
	if( oneway )
	{
		$('#returnDate').hide();
		refreshResults();
	}
	else
	{
		$('#returnDate').show();
		//This will result in a call to refreshResults():
		adjustMinimumReturnDate();
	}
}

function onOriginChange()
{
	console.log('onOriginChange');
}

function loadAirportCodes()
{
	$(document).ready(function() {
        $.ajax({
            url: "/airportCodes/"
        }).then(function(data) {
        	console.log("Loaded " + data.length + " airport codes!");
        	$( "body" ).data( "airports", data );
        	suggestAutoComplete( $('#origin') );
        	suggestAutoComplete( $('#destination') );
        });
    });
}

function suggestAutoComplete(field)
{
	var airports = $( "body" ).data( "airports" );
	field.autocomplete({
		delay: 0,
		minLength: 2,
		autoFocus: true,
		source: function(request, response) {
			var results = [];
			var index;
			if( request.term.length <= 3 )
			{
				for(index in airports)
				{
					if( airports[index].startsWith(request.term.toUpperCase()) )
					{
						results.push(airports[index]);
						if( results.length >= 10 )
						{
							break;
						}
					}
				}
			}
			if( request.term.length >= 3 )
			{
				var filtered = $.ui.autocomplete.filter(airports, request.term);
				for(index in filtered)
				{
					if( !filtered[index].startsWith(request.term.toUpperCase()) )
					{
						results.push(filtered[index]);
						if( results.length >= 10 )
						{
							break;
						}
					}
				}
			}
			response(results);
		}
	});
}

function refreshResults()
{
	load([]);
	if( $('#oneway').is(':checked') )
	{
		$('#table').bootstrapTable('hideColumn', 'return');
	}
	else
	{
		$('#table').bootstrapTable('showColumn', 'return');
	}
    var departureDate = getDate( $('#departureDate') );
    var returnDate = getDate( $('#returnDate') );
    if( $('#origin').val() != null )
    {
    	var originTyped = $('#origin').val().toUpperCase();
    }
    if( $('#destination').val() != null )
    {
    	var destinationTyped = $('#destination').val().toUpperCase();
    }
    var origin;
    var destination;
    if( originTyped && destinationTyped )
    {
		var airports = $( "body" ).data( "airports" );
		for(index in airports)
		{
			if( originTyped === airports[index].substring(0, 3) )
			{
				origin = airports[index].substring(0, 3);
			}
			if( destinationTyped === airports[index].substring(0, 3) )
			{
				destination = airports[index].substring(0, 3);
			}
		}
		if( origin && destination && departureDate && ($('#oneway').is(':checked') || returnDate) )
		{
			var formattedDepartureDate = $.datepicker.formatDate('yymmdd', departureDate);

			if( $('#oneway').is(':checked') )
			{
				var arguments = {origin: origin, destination: destination, departureDate: formattedDepartureDate};
			}
			else
			{
				var formattedReturnDate = $.datepicker.formatDate('yymmdd', returnDate);
				var arguments = {origin: origin, destination: destination, departureDate: formattedDepartureDate, returnDate: formattedReturnDate};
			}
			var url = "/query?" + $.param( arguments );
			var queryTimestamp = new Date().getTime();
			$(document).ready(function() {
				$.ajax({
					url: url
				}).then(function(data) {
					console.log( 'Server query call took ' + (new Date().getTime() - queryTimestamp) + ' milliseconds' );
					load(data);
				});
			});
		}
    }
}

function initTable() {
	console.log( "Initializing table" );
	var table = $('#table');
	table.bootstrapTable({
		showHeader: false,
		striped: true,
		pagination: true,
		pageSize: 50,
		pageList: []
	});
	table.on("click-row.bs.table", function(e, itinerary, $tr) {

		var rowIndex = $tr[0].getAttribute("data-index", 1);
		// Trigger to expand row with text detailFormatter..?
		if ($tr.next().is('tr.detail-view'))
		{
			table.bootstrapTable('collapseRow', rowIndex);
		}
		else
		{
			table.bootstrapTable('expandRow', rowIndex);
		}
	});
}

function load(data) {
	console.log( "Initializing table with " + data.length + " itineraries" );
	var table = $('#table');
	table.bootstrapTable("load", data);
	if( data != null && data.length > 0 )
	{
		$('#table-container').show();
	}
	else
	{
		$('#table-container').hide();
	}
}

function showDetails(index, itinerary, element)
{
	var details = "<br/>";
	for(flightIndex in itinerary.flights )
	{
		if( flightIndex > 0 )
		{
			details += "<br/><br/>";
		}
		var flight = itinerary.flights[flightIndex];
		details += "<span class=\"bold\">";
		if( itinerary.flights.length == 1 )
		{
			details += "One way flight:";
		}
		else if( itinerary.flights.length == 2 )
		{
			if( flightIndex == 0 )
			{
				details += "Departing flight:";
			}
			else
			{
				details += "Returning flight:";
			}
		}
		else
		{
			details += "Flight #" + (flightIndex + 1) + ":";
		}
		details += "</span>";
		for(segmentIndex in flight.segments )
		{
			if( segmentIndex >= 0 )
			{
				details += "<br/>";
			}
			var segment = flight.segments[segmentIndex];
			details += "&nbsp;&nbsp;&nbsp;Flight #" + segment.flightNumber;
			details += ", " + segment.departureAirport + " - " + segment.arrivalAirport;
			details += "  " + segment.formattedDepartureTime + " - " + segment.formattedArrivalTime;
		}
	}
	details += "<br/>&nbsp;";
	return details;
}

function getSegmentString(segment)
{
	return printout;
}

function departureFormatter(value, itinerary)
{
	return getFlightSummary( itinerary.flights[0] );
}

function returnFormatter(value, itinerary)
{
	if( itinerary.flights.length == 1 )
	{
		return '';
	}
	else
	{
		return getFlightSummary( itinerary.flights[1] );
	}
}

function getFlightSummary(flight)
{
	var initialDeparture;
	var finalArrival;
	var stops;
	for( segmentIndex in flight.segments )
	{
		var segment = flight.segments[segmentIndex];
		if( segmentIndex == 0 )
		{
			initialDeparture = segment.formattedDepartureTime;
			stops = 'Nonstop';
		}
		else if( segmentIndex == 1 )
		{
			stops = segmentIndex + ' stop';
		}
		else
		{
			stops = segmentIndex + ' stops';
		}
		finalArrival = segment.formattedArrivalTime;
	}
	stops = padLeft(stops, 7);
	initialDeparture = padLeft(initialDeparture, 7);
	finalArrival = padLeft(finalArrival, 7);
	var duration = getFormattedDuration( flight.duration );
	return initialDeparture + " - " + finalArrival + " &nbsp;&nbsp;&nbsp;&nbsp; " + duration + " &nbsp;&nbsp;&nbsp;&nbsp; " + stops;
}

function padLeft( string, length )
{
	var pads = length - string.length;
	while( pads-- )
	{
		string = '&nbsp;' + string;
	}
	return string;
}

function getFormattedDuration( duration )
{
	var hours = Math.floor( duration / 60 );
	var minutes = duration % 60;
	var formattedDuration = '';
	if( hours > 0 )
	{
		formattedDuration = hours + 'h';
	}
	if( minutes < 10 )
	{
		formattedDuration += '0' + minutes + 'm';
	}
	else
	{
		formattedDuration += minutes + 'm';
	}
	return padLeft(formattedDuration, 6);
}

function rowStyle(row, index) {
  return {
    css: {"padding-top": "25px;", "padding-right": "70px;", "padding-bottom": "25px;", "padding-left": "70px;", "border-radius": "0px"}
  };
}